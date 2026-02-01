package aibus;

import controllers.SimAIBus;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListenerWithExceptions;
import com.fazecast.jSerialComm.SerialPortEvent;

public class AIBusHandler {
	private SimAIBus controller;
	
	private SerialPort[] all_ports;
	public SerialPort active_port = null;
	
	private AIData[] rx_message_list, tx_message_list;
	private AIBusHandler self;
	private AIListener ai_listener;

	private ScreenReceiverGroup receiver_group;
	private ReceiverTimer receiver_timer;

	private ArrayList<Byte> ack_ids; //The acknowledgment ID list.
	private boolean acknowledge_on = true;
	
	private static final int ai_delay = 1, ai_wait = 5;

	private static final short aidata_limit = 0x20;

	private long last_received_msg = 0;
	
	public AIBusHandler(SimAIBus controller) {
		this.controller = controller;
		this.rx_message_list = new AIData[0];
		this.tx_message_list = new AIData[0];

		this.ai_listener = new AIListener(new AIBusCache(null), this);

		this.ack_ids = new ArrayList<Byte>(0);

		this.receiver_group = new ScreenReceiverGroup();
		this.receiver_timer = new ReceiverTimer(this.receiver_group);

		this.last_received_msg = System.currentTimeMillis();
		
		refreshAllPorts();

		self = this;
	}
	
	//Serial port stuff:
	/** Refresh the serial port list. */
	public void refreshAllPorts() {
		all_ports = SerialPort.getCommPorts();
		
		if(active_port != null) {
			int index = -1;
			for(int i=0;i<all_ports.length;i+=1) {
				if(all_ports[i].getSystemPortName().equals(active_port.getSystemPortName())) {
					index = i;
					break;
				}
			}
			
			if(index >= 0 && index < all_ports.length) 
				active_port = all_ports[index];
			else
				active_port = null;
		}
	}
	
	/** Get the serial port list. */
	public SerialPort[] getAllPorts() {
		return this.all_ports;
	}
	
	/** Get the index of the desired serial port. */
	public int getPortIndex(SerialPort desired) {
		int index = -1;
		for(int i=0;i<all_ports.length;i+=1) {
			if(all_ports[i].equals(desired)) {
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/** Activate the desired serial port. */
	public void activatePort(SerialPort port) {
		if(port.isOpen()) {
			final int rd = port.bytesAvailable();
			byte[] d = new byte[rd];
			port.readBytes(d, rd);

			this.ai_listener.setPort(port);

			//port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 20, port.getWriteTimeout());
			if(port != this.active_port)
				this.active_port = port;

			this.receiver_timer.run = true;
			
			Thread receiver_thread = new Thread(receiver_timer);
			receiver_thread.start();
		}
	}
	
	/** Deactivate the desired serial port. */
	public void deactivatePort(SerialPort port) {
		port.removeDataListener();
		this.receiver_timer.run = false;
	}
	
	//AIBus stuff:
	/** Write an AIBus message to the serial port. */
	public int sendAIBusMessage(AIData the_message) {
		if(the_message.l > aidata_limit + 3) {
			final int count = the_message.l/aidata_limit, r = the_message.l%aidata_limit;

			AIData[] ai_group = new AIData[count + (r == 0 ? 0 : 1)];

			for(int i=0;i<ai_group.length;i+=1) {
				final int l = i < ai_group.length - 1 || r == 0 ? aidata_limit + 3 : r + 3;
				byte[] ai_data = new byte[l];
				ai_data[0] = (byte)0x91;
				ai_data[1] = (byte)ai_group.length;
				ai_data[2] = (byte)i;
				for(int d=0;d<l-3;d+=1)
					ai_data[d+3] = the_message.data[aidata_limit*i+d];

				ai_group[i] = new AIData((short)l, the_message.sender, the_message.receiver, ai_data);
			}

			int sent = 0;

			for(int i=0;i<ai_group.length;i+=1) {
				final int last_sent = sendAIBusMessage(ai_group[i]);
				if(last_sent < 0)
					return -1;

				sent += last_sent;
				
				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					
				}
			}
			return sent;
		}

		if(active_port == null)
			return -1;
		
		if(!active_port.isOpen()) {
			return -1;
			/*final boolean success = active_port.openPort();
			if(!success)
				return -1;*/
			
		}

		AIBusCache byte_cache = this.ai_listener.cache;
		byte_cache.fill();
		
		while(System.currentTimeMillis() - last_received_msg < ai_delay);

		byte[] data = the_message.getBytes();
		final int bytes_written = active_port.writeBytes(data, data.length);

		active_port.setRTS();

		return bytes_written;
	}
	
	/** Write an AIBus message from the TX list to the serial port. */
	public int sendAIBusMessage(final int message_index) {
		if(message_index >= 0) 
			return sendAIBusMessage(this.tx_message_list[message_index]);
		else
			return -1;
	}

	/** Get an array of AIBus messages from a byte stream. */
	private AIData[] getAIBusMessage(byte[] stream) {
		final int total_length = stream.length;
		
		int h = 0, message = 0;
		AIData[] data_collection = new AIData[255];
		
		for(int i=0;i<stream.length;i+=1)
			System.out.print(Integer.toHexString(stream[i]&0xFF) + " ");
		System.out.println();
		
		while(h<total_length) {
			//int start = h;
			try {
				final byte sender = stream[h+0], receiver = stream[h+2];
				final int l = stream[h+1]&0xFF;
				
				byte data[] = new byte[l+2];
				data[0] = sender;
				data[1] = (byte)l;
				data[2] = receiver;

				for(int i=0;i<l-1;i+=1)
					data[i+3] = stream[h+i+3];

				h+=l+3;

				if(AIData.checkValidity(data)) {
					AIData new_msg = new AIData((short) (l-2), sender, receiver);
				
					new_msg.sender = sender;
					new_msg.receiver = receiver;
					
					for(int i=0;i<l-2;i+=1)
						new_msg.data[i] = data[i+3];

					data_collection[message] = new_msg;
				} else {
					h -= l+3;
					int sh = h+1;

					boolean valid = false;
					while(!valid && sh + 4 < total_length) {
						final byte s = stream[sh], r = stream[sh+2];
						final int newl = stream[sh+1]&0xFF;

						if(newl < 2 || newl > aidata_limit + 5 || newl > total_length - (sh + 4)) {
							sh += 1;
							continue;
						}

						final int end_sh = sh + newl+3;

						byte[] new_data = new byte[newl + 2];

						for(int i=sh;i<end_sh && i-sh<new_data.length;i+=1)
							new_data[i-sh] = stream[i];

						if(AIData.checkValidity(new_data)) {
							AIData new_msg = new AIData((short) (newl-2), s, r);
				
							new_msg.sender = s;
							new_msg.receiver = r;
							
							for(int i=0;i<newl-2;i+=1)
								new_msg.data[i] = new_data[i+3];

							data_collection[message] = new_msg;

							valid = true;
							h = end_sh;
							break;
						}

						sh += 1;
					}

					if(!valid) //Something went wrong.
						break;
				}
				
				message += 1;
			} catch(ArrayIndexOutOfBoundsException e) {
				if(total_length-h > 2) {
					//TODO: There was an error.
					break;
				} else
					h = total_length;
				break;
			}
		}
		
		AIData[] the_return = new AIData[message];
		for(int i=0;i<the_return.length;i+=1)
			the_return[i] = data_collection[i];
			
		return the_return;
		
	}
	
	/** Add an AIBus message to the RX list. */
	private void addAIBusMessageRx(AIData the_message) {
		AIData[] new_list = new AIData[rx_message_list.length + 1];
		
		for(int i=0;i<rx_message_list.length;i+=1)
			new_list[i] = rx_message_list[i];
		
		new_list[rx_message_list.length] = the_message;
		rx_message_list = new_list;
	}
	
	/** Clear the RX list. */
	public void clearRXList() {
		this.rx_message_list = new AIData[0];
	}
	
	/** Add an AIBus message to the TX list. */
	public void addAIBusMessageTx(AIData the_message) {
		AIData[] new_list = new AIData[tx_message_list.length + 1];
		
		for(int i=0;i<tx_message_list.length;i+=1)
			new_list[i] = tx_message_list[i];
		
		new_list[tx_message_list.length] = the_message;
		tx_message_list = new_list;
		
		controller.getMainWindow().addTxMessageToWindow(the_message);
	}
	
	/** Remove an AIBus message from the TX list. */
	public void removeAIBusMessageTx(final int index) {
		if(tx_message_list.length <= 0)
			return;
		
		AIData[] new_list = new AIData[tx_message_list.length - 1];
		
		for(int i=0;i<new_list.length;i+=1) {
			if(i<index)
				new_list[i] = tx_message_list[i];
			else
				new_list[i] = tx_message_list[i+1];
		}
		
		tx_message_list = new_list;
		
		controller.getMainWindow().removeTxMessage(index);
	}

	/** Clear the TX list. */
	public void clearAIBusMessagesTx() {
		this.tx_message_list = new AIData[0];
	}

	/** Read an AIBus message from the serial cache. */
	private void readAIBusMessage(AIBusCache cache) {
		if(cache == null)
			return;

		while(cache.bytesAvailable() >= 4) {
			try {
				cache.fill();
				if(cache.bytesAvailable() < 4) {
					long start_time = System.nanoTime()/1000000;
					int avail = cache.bytesAvailable();
					while(System.nanoTime()/1000000 - start_time < ai_wait) {
						cache.fill();
						if(cache.bytesAvailable() != avail) {
							avail = cache.bytesAvailable();
							start_time = System.nanoTime()/1000000;
						}
					}
					
					if(cache.bytesAvailable() < 4) {
						final int dl = cache.bytesAvailable();
						byte[] db = new byte[dl];
						cache.readBytes(db, dl);
						return;
					}
				}
			
				byte[] init = new byte[2];		
				cache.readBytes(init, 2);

				final int l = init[1]&0xFF;

				if(l > aidata_limit + 5 || l < 2)
					return;
				
				final long start_time = System.nanoTime()/1000000;
				while(cache.bytesAvailable() < l) {
					cache.fill();
					if(System.nanoTime()/1000000 - start_time >= ai_wait) {
						final int dl = cache.bytesAvailable();
						byte[] db = new byte[dl];
						cache.readBytes(db, dl);
						return;
					}
				}

				byte[] b = new byte[(int)(init[1]&0xFF) + 2];
				b[0] = init[0];
				b[1] = init[1];
				cache.readBytes(b, (int)(init[1]&0xFF), 2);
				AIData[] message_list = self.getAIBusMessage(b);
				
				for(int i=0;i<message_list.length;i+=1) {
					AIData rec_message = message_list[i];
					if(rec_message != null && rec_message.l >= 1) {
						addAIBusMessageRx(rec_message);
						controller.getMainWindow().addRxMessageToWindow(rec_message);

						if(acknowledge_on) {
							//Acknowledgment list.
							for(int a=0;a<ack_ids.size();a+=1) {
								if(rec_message.receiver == ack_ids.get(a) && rec_message.l >= 1 && rec_message.data[0] != 0x80) {
									final byte id = ack_ids.get(a);

									byte[] ack_data = {(byte)0x80};
									AIData ack_msg = new AIData(ack_data, id, rec_message.sender);

									this.sendAIBusMessage(ack_msg);
									
									break;
								}
							}

							if(this.receiver_group.screen_emulator_on && rec_message.receiver == 0x7) {
								if(rec_message.l >= 1 && rec_message.data[0] != (byte)0x80) {
									byte[] ack_data = {(byte)0x80};
									AIData ack_msg = new AIData(ack_data, (byte)0x7, rec_message.sender);

									this.sendAIBusMessage(ack_msg);
								}

								if(rec_message.l == 2 && rec_message.data[0] == 0x31 && rec_message.data[1] == 0x30)
									this.sendScreenButtons(rec_message.sender);
							}
						}

						if(rec_message.receiver == 0x7 && rec_message.l >= 3 && rec_message.data[0] == 0x77)
							resetReceiverTimers(rec_message.data[1], rec_message.data[2]);
					}
				}
			} catch(ArrayIndexOutOfBoundsException | NullPointerException e) {
				return;
			}
		}
		
		if(cache.bytesAvailable() > 0) {
			final long start_time = System.nanoTime()/1000000;
			while(System.nanoTime()/1000000 - start_time < ai_wait) {
				if(cache.bytesAvailable() >= 4)
					return;
			}
			
			final int dl = cache.bytesAvailable();
			byte[] db = new byte[dl];
			cache.readBytes(db, dl);
		}
	}

	/** Reset the screen receiver timers. */
	private void resetReceiverTimers(final byte source, final byte mode) {
		if((mode&0x80) != 0) {
			this.receiver_group.source_receiver = source;
			this.receiver_group.source_receiver_set = true;
			this.receiver_timer.last_source_timer = System.nanoTime()/1000000;
		}
		if((mode&0x20) != 0) {
			this.receiver_group.audio_receiver = source;
			this.receiver_group.audio_receiver_set = true;
			this.receiver_timer.last_audio_timer = System.nanoTime()/1000000;

			if(!this.receiver_group.source_receiver_set || this.receiver_group.source_receiver == this.receiver_group.audio_receiver) {
				this.receiver_group.source_receiver = source;
				this.receiver_group.source_receiver_set = true;
				this.receiver_timer.last_source_timer = System.nanoTime()/1000000;
			}
		}
		if((mode&0x10) != 0) {
			this.receiver_group.main_receiver = source;
			this.receiver_group.main_receiver_set = true;
			this.receiver_timer.last_main_timer = System.nanoTime()/1000000;
			
			if(!this.receiver_group.audio_receiver_set || this.receiver_group.audio_receiver == this.receiver_group.main_receiver) {
				this.receiver_group.audio_receiver = source;
				this.receiver_group.audio_receiver_set = true;
				this.receiver_timer.last_audio_timer = System.nanoTime()/1000000;

				if(!this.receiver_group.source_receiver_set || this.receiver_group.source_receiver == this.receiver_group.audio_receiver) {
					this.receiver_group.source_receiver = source;
					this.receiver_group.source_receiver_set = true;
					this.receiver_timer.last_source_timer = System.nanoTime()/1000000;
				}
			}
		}
	}

	/** Send the supported button message as the screen. */
	private void sendScreenButtons(final byte receiver) {
		byte[] button_data = {0x31, 0x30, 0x6, 0x20, 0x23, 0x36, 0x37, 0x34, 0x35, 0x26, 0x25, 0x24, 0x6, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x28, 0x29, 0x2A, 0x2B, 0x7, 0x50, 0x51, 0x52, 0x53, 0x54, 0x27};
		AIData button_msg = new AIData(button_data, (byte)0x7, receiver);

		int bytes_sent = 0;
		do {
			bytes_sent = this.sendAIBusMessage(button_msg);
		} while(bytes_sent <= 0);
	}
	
	/** Set whether to acknowledge any messages sent to a device on the ack ID list.*/
	public void setAcknowledge(final boolean acknowledge) {
		this.acknowledge_on = acknowledge;
	}

	/** Get the ack ID list. */
	public ArrayList<Byte> getAckIDs() {
		return this.ack_ids;
	}

	/** Get the main controller. */
	public SimAIBus getController() {
		return this.controller;
	}

	/** Get the screen receiver IDs. */
	public ScreenReceiverGroup getReceiverGroup() {
		return this.receiver_group;
	}

	/** Get the TX message list. */
	public AIData[] getTxMessageList() {
		return this.tx_message_list;
	}
	
	/** Get the RX message list. */
	public AIData[] getRxMessageList() {
		return this.rx_message_list;
	}

	/** Add the list of RX messages to a table. */
	public void addRxAIMessagesToTable(JTable table, boolean clear) {
		DefaultTableModel table_model;
		if(table.getModel() instanceof DefaultTableModel)
			table_model = (DefaultTableModel)table.getModel();
		else
			return;
		
		if(clear) {
			for(int i=0;i<table.getRowCount();i+=1)
				table_model.removeRow(i);
		}
		
		for(int i=0;i<this.rx_message_list.length;i+=1)
			addAIMessageToTable(table_model, rx_message_list[i], -1);
	}
	
	/** Add an AIBus message to a table. */
	public static void addAIMessageToTable(JTable table, AIData ai_d) {
		DefaultTableModel table_model;
		if(table.getModel() instanceof DefaultTableModel)
			table_model = (DefaultTableModel)table.getModel();
		else
			return;
		
		addAIMessageToTable(table_model, ai_d, -1);
	}

	/** Set a table row to an AIBus message. */
	public static void setTableAIMessage(JTable table, AIData ai_d, final int row) {
		DefaultTableModel table_model;
		if(table.getModel() instanceof DefaultTableModel)
			table_model = (DefaultTableModel)table.getModel();
		else
			return;
		
		addAIMessageToTable(table_model, ai_d, row);
	}
	
	/** Add an AIBus message to a table at a specified row. */
	private static void addAIMessageToTable(DefaultTableModel table_model, AIData ai_d, final int row) {
		String data_string = "", ascii_string = "";
		for(int i=0;i<ai_d.data.length; i+=1) {
			data_string += Integer.toHexString(ai_d.data[i]&0xFF);
			if(i < ai_d.data.length - 1)
				data_string += " ";

			if((int)(ai_d.data[i]&0xFF) >= 0x20)
				ascii_string += (char)ai_d.data[i];
		}
		
		data_string = data_string.toUpperCase();
		if(row < 0)
			table_model.addRow(new String[] {Integer.toHexString(ai_d.sender&0xFF).toUpperCase(),
										 Integer.toHexString(ai_d.l).toUpperCase(),
										 Integer.toHexString(ai_d.receiver&0xFF).toUpperCase(), data_string, ascii_string});
		else {
			table_model.setValueAt(Integer.toHexString(ai_d.sender&0xFF).toUpperCase(), row, 0);
			table_model.setValueAt(Integer.toHexString(ai_d.l).toUpperCase(), row, 1);
			table_model.setValueAt(Integer.toHexString(ai_d.receiver&0xFF).toUpperCase(), row, 2);
			table_model.setValueAt(data_string, row, 3);
			table_model.setValueAt(ascii_string, row, 4);
		}
	}

	private final class AIListener implements SerialPortDataListenerWithExceptions {
		private SerialPort port;

		private AIBusCache cache;
		private AIBusHandler parent;

		private AIListener(AIBusCache cache, AIBusHandler parent) {
			this.cache = cache;
			this.port = cache.port;
			this.parent = parent;

		}

		/** Set the serial port. */
		private void setPort(SerialPort port) {
			this.port = port;
			this.cache.setPort(port);

			if(port != null) {
				port.addDataListener(this);
			}
		}

		@Override
		public int getListeningEvents() {
			return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
		}

		@Override
		public void serialEvent(SerialPortEvent arg0) {
			if(arg0.getSerialPort() != this.port)
				return;

			int avail = port.bytesAvailable();

			if(avail > 0)
				last_received_msg = System.currentTimeMillis();

			this.cache.fill();
			while(System.currentTimeMillis() - last_received_msg < ai_wait) {
				if(port.bytesAvailable() != avail) {
					avail = port.bytesAvailable();
					last_received_msg = System.currentTimeMillis();
					this.cache.fill();
				}
			}
			
			parent.readAIBusMessage(cache);
		}

		@Override
		public void catchException(Exception arg0) {	
			String error_message = "An error has occurred. The following details are provided: ";
			error_message += arg0.toString();
			JOptionPane.showMessageDialog(controller.getMainWindow(), error_message, "Error", JOptionPane.ERROR_MESSAGE);
			//TODO: Close/reopen the serial port.
		}
	}

	/*private final class AIListenerThread implements Runnable {
		private SerialPort port;
		private boolean run = false;
		private AIBus_Handler parent;

		private AIBusCache cache;

		private AIListenerThread(AIBus_Handler parent, AIListener listener) {
			this.port = null;
			this.parent = parent;

			this.cache = listener.cache;
		}

		private void start(SerialPort port) {
			this.port = port;
			this.cache.setPort(port);
			this.run = true;
			new Thread(this).start();
		}

		private void stop() {
			this.run = false;
		}

		@Override
		public void run() {
			while(this.run) {
				try {
					if(this.port == null) {
						run = false;
						return;
					}

					if(this.port.bytesAvailable() > 0) {
						long start_time = System.nanoTime()/1000000;
						while(this.port.bytesAvailable() < this.port.getDeviceReadBufferSize() - 5 && System.nanoTime()/1000000 - start_time < ai_wait);
						cache.fill();
					}
				} catch(NullPointerException e) {
					continue;
				}

				if(cache.data.size() > 0)
					parent.readAIBusMessage(cache);
			}
		}
		
	}*/

	private final class ReceiverTimer implements Runnable {
		public long last_main_timer = System.nanoTime()/1000000, last_audio_timer = System.nanoTime()/1000000, last_source_timer = System.nanoTime()/1000000;

		private ScreenReceiverGroup receiver_group;
		private boolean run = false;

		private ReceiverTimer(ScreenReceiverGroup receiver_group) {
			this.receiver_group = receiver_group;
		}

		@Override
		public void run() {
			while(run) {
				final long current_time = System.nanoTime()/1000000;
				if(receiver_group.main_receiver_set && current_time - last_main_timer > 5000) {
					receiver_group.main_receiver_set = false;
					receiver_group.main_receiver = 0x1;
				}

				if(receiver_group.audio_receiver_set && current_time - last_audio_timer > 5000) {
					receiver_group.audio_receiver_set = false;
					
					if(receiver_group.main_receiver_set)
						receiver_group.audio_receiver = receiver_group.main_receiver;
					else
						receiver_group.audio_receiver = 0x1;
				}

				if(receiver_group.source_receiver_set && current_time - last_source_timer > 5000) {
					receiver_group.source_receiver_set = false;

					if(receiver_group.audio_receiver_set)
						receiver_group.source_receiver = receiver_group.audio_receiver;
					else if(receiver_group.main_receiver_set)
						receiver_group.source_receiver = receiver_group.main_receiver;
					else
						receiver_group.source_receiver = 0x1;
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					continue;
				}
			}
		}
	}

	private final class AIBusCache {
		private ArrayList<Byte> data;
		private SerialPort port;

		private AIBusCache(SerialPort port) {
			this.port = port;
			this.data = new ArrayList<Byte>(0);
		}

		/** Set the receiving serial port. */
		private void setPort(SerialPort port) {
			this.port = port;
		}

		/** Fill the cache with data from the serial port. */
		private void fill() {
			if(this.port == null)
				return;

			final int l = this.port.bytesAvailable();
			if(l <= 0)
				return;

			byte[] data = new byte[l];
			this.port.readBytes(data, l);

			for(int i=0;i<l;i+=1)
				this.data.add(Byte.valueOf(data[i]));
		}

		/** Get the number of bytes available in the cache. */
		private int bytesAvailable() {
			return this.data.size();
		}

		/** Read bytes from the cache. */
		private void readBytes(byte[] d, int l) {
			readBytes(d, l, 0);
		}

		/** Read bytes from the cache. */
		private void readBytes(byte[] d, int l, final int offset) {
			if(d.length < l)
				return;

			int index = offset;
			while(index - offset < l && index < d.length) {
				d[index] = (byte)(this.data.get(0));
				this.data.remove(0);
				index += 1;

				if(index >= d.length)
					break;
			}
		}
	}
}
