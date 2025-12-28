package controllers;

//import java.awt.GraphicsEnvironment;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import com.fazecast.jSerialComm.SerialPort;

import aibus.AIBusHandler;
import aibus.AIData;
import main_window.SimAIBusMainWindow;

public class SimAIBus {
	private SimAIBusMainWindow main_window;
	private AIBusHandler aibus_handler;
	private SaveLoadController save_load_controller;
	
	private boolean split_data = false;

	private int refresh_delay = 0;
	
	public static void main(String args[]) {
		boolean use_native_ui = false;
		for(int i=0;i<args.length;i+=1) {
			if(args[i].equalsIgnoreCase("-NATIVE")) {
				use_native_ui = true;
				break;
			}
		}

		try {
			try {
				if(!System.getProperty("os.name").equalsIgnoreCase("LINUX") || use_native_ui) {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					//System.out.println(UIManager.getSystemLookAndFeelClassName());
				} else {
					LookAndFeelInfo[] lf_info = UIManager.getInstalledLookAndFeels();
					int lf_index = -1;
					for(int i=0;i<lf_info.length;i+=1) {
						if(lf_info[i].getClassName().toLowerCase().contains("nimbus")) {
							lf_index = i;
							break;
						}
					}

					if(lf_index >= 0)
						UIManager.setLookAndFeel(lf_info[lf_index].getClassName());
					else
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (UnsupportedLookAndFeelException e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				e1.printStackTrace();
			}
		}
		
		new SimAIBus();
	}
	
	public SimAIBus() {
		this.aibus_handler = new AIBusHandler(this);
		this.save_load_controller = new SaveLoadController(this);

		SimAIBus self = this;
		this.refresh_delay = 16;
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				self.main_window = new SimAIBusMainWindow(self);
			}
		});
	}
	
	/** Get the active serial port. */
	public SerialPort getActivePort() {
		return this.aibus_handler.active_port;
	}
	
	/** Set the active serial port. */
	public void setActivePort(SerialPort port) {
		this.aibus_handler.active_port = port;
	}
	
	/** Set the active serial port. */
	public void setActivePort(int port) {
		this.aibus_handler.active_port = this.aibus_handler.getAllPorts()[port];
	}
	
	/** Get the index of the desired port. */
	public int getPortIndex(SerialPort desired) {
		return this.aibus_handler.getPortIndex(desired);
	}
	
	/** Get all available serial ports. */
	public SerialPort[] getAllPorts() {
		this.aibus_handler.refreshAllPorts();
		return this.aibus_handler.getAllPorts();
	}
	
	/** Get whether data is split by default. */
	public boolean getSplitData() {
		return this.split_data;
	}
	
	/** Set whether data is split by default. */
	public void setSplitData(final boolean split_data) {
		this.split_data = split_data;
	}
	
	/** Get the AIBus handler. */
	public AIBusHandler getCommunicator() {
		return this.aibus_handler;
	}
	
	/** Get the main window. */
	public SimAIBusMainWindow getMainWindow() {
		return this.main_window;
	}

	/** Save the TX list. */
	public void activateTxListSave() {
		this.save_load_controller.openSaveChooser(this.aibus_handler.getTxMessageList());
	}

	/** Save the RX list. */
	public void activateRxListSave() {
		this.save_load_controller.openSaveChooser(this.aibus_handler.getRxMessageList());
	}

	/** Load a TX list. */
	public void activateTxListLoad() {
		AIData[] message_list = this.save_load_controller.openLoadChooser();

		if(message_list.length > 0) {
			this.aibus_handler.clearAIBusMessagesTx();
			this.main_window.clearTxTable();
			for(int i=0;i<message_list.length;i+=1)
				this.aibus_handler.addAIBusMessageTx(message_list[i]);
		}
	}

	/** Get the monitor refresh rate. */
	public int getRefreshRate() {
		return this.refresh_delay;
	}
	
	public void endProgram() {
		//TODO: Anything?
	}
}
