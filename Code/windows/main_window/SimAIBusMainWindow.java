package main_window;

import java.awt.Dimension;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.JFrame;

import controllers.SimAIBus;
import tools.EditAIBusWindow;
import tools.ScreenEmulatorWindow;

import javax.swing.JPanel;

import com.fazecast.jSerialComm.SerialPort;

import aibus.*;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.ComponentOrientation;
import javax.swing.JScrollPane;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;

public class SimAIBusMainWindow extends JFrame {

	private static final long serialVersionUID = 582725181396857875L;
	private static final int w_width = 800, w_height = 600;
	private static final String str_connect = "Connect", str_disconnect = "Close";
	
	private static final int message_table_from_edge = w_width - 778;

	private static final byte standard_aibus = 1, standard_ibus = 2;
	
	private SimAIBus controller;
	private JTable rxMessageTable;
	private JTable txMessageTable;
	private JFrame sub_window = null;
	private final ButtonGroup busStandardGroup = new ButtonGroup();

	private JScrollPane rxMessageTableScroll;
	private boolean autoscroll = true;

	private long last_frame_timer;
	
	public SimAIBusMainWindow(SimAIBus controller) {
		this.controller = controller;
		SimAIBusMainWindow self = this;

		this.last_frame_timer = System.currentTimeMillis();
		
		AIBusHandler handler = this.controller.getCommunicator();
		
		this.setTitle("SimAIBus");
		this.setFocusable(true);
		this.setResizable(true);
		
		this.getContentPane().setPreferredSize(new Dimension(w_width, w_height));
		this.getContentPane().setMinimumSize(new Dimension(w_width, w_height));
		this.getContentPane().setSize(this.getContentPane().getPreferredSize());
		this.pack();
		
		//TODO: Set minimum size?
		
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.getContentPane().setLayout(null);
		
		JPanel serialControlPanel = new JPanel();
		serialControlPanel.setBounds(0, 0, this.getContentPane().getWidth(), 98);
		getContentPane().add(serialControlPanel);
		serialControlPanel.setLayout(null);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(12, 12, 35, 27);
		serialControlPanel.add(lblPort);
		
		JButton btnConnect = new JButton(str_connect);
		btnConnect.setToolTipText("Start or end the serial connection.");
		btnConnect.setBounds(280, 12, 117, 27);
		serialControlPanel.add(btnConnect);
		
		JComboBox<String> serialComboBox = new JComboBox<String>();
		serialComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(self.controller.getActivePort() == null) {
					//Do nothing.
				} else if(self.controller.getActivePort().isOpen() && self.controller.getPortIndex(self.controller.getActivePort()) != serialComboBox.getSelectedIndex()) {
					self.controller.getActivePort().closePort();
					self.controller.getCommunicator().deactivatePort(self.controller.getActivePort());
					btnConnect.setText(str_connect);
				}
				
				if(serialComboBox.getSelectedIndex() >= 0) {
					self.controller.setActivePort(self.controller.getAllPorts()[serialComboBox.getSelectedIndex()]);
					btnConnect.setEnabled(true);
				} else {
					self.controller.setActivePort(null);
					btnConnect.setEnabled(false);
				}
			}
		});
		serialComboBox.setToolTipText("Select the serial port on which the AIBus data is to be received and sent.");
		serialComboBox.setBounds(55, 7, 213, 32);
		serialControlPanel.add(serialComboBox);
		
		serialComboBoxRefresh(serialComboBox, btnConnect);
		
		JButton btnRefreshList = new JButton("Refresh List");
		btnRefreshList.setToolTipText("Refresh the list of serial ports.");
		btnRefreshList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serialComboBoxRefresh(serialComboBox, btnConnect);
			}
		});
		btnRefreshList.setBounds(55, 48, 119, 27);
		serialControlPanel.add(btnRefreshList);
		
		JButton btnClearTable = new JButton("Clear Rx Table");
		btnClearTable.setToolTipText("Clear the table of AIBus messages.");
		btnClearTable.setBounds(280, 49, 134, 27);
		serialControlPanel.add(btnClearTable);
		
		JRadioButton rdbtnAibusn = new JRadioButton("AIBus (115200 @ 8N1)");
		rdbtnAibusn.setToolTipText("Connect to an AIBus network with a baudrate of 115200bps and no parity.");
		rdbtnAibusn.setSelected(true);
		busStandardGroup.add(rdbtnAibusn);
		rdbtnAibusn.setBounds(460, 5, 190, 27);
		serialControlPanel.add(rdbtnAibusn);
		
		JRadioButton rdbtnIbus = new JRadioButton("IBus (9600 @ 8E1)");
		rdbtnIbus.setToolTipText("Connect to an IBus network with a baudrate of 9600bps and even parity.");
		busStandardGroup.add(rdbtnIbus);
		rdbtnIbus.setBounds(460, 33, 190, 27);
		serialControlPanel.add(rdbtnIbus);
		
		JCheckBox chckbxAutoscroll = new JCheckBox("Autoscroll");
		chckbxAutoscroll.setToolTipText("Check to allow the received message table to scroll automatically as new messages are added.");
		chckbxAutoscroll.setSelected(autoscroll);
		chckbxAutoscroll.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				self.autoscroll = chckbxAutoscroll.isSelected();
			}
		});
		chckbxAutoscroll.setBounds(460, 61, 190, 27);
		serialControlPanel.add(chckbxAutoscroll);
		
		JScrollPane rxMessageTableScroll = new JScrollPane();
		rxMessageTableScroll.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		rxMessageTableScroll.setBounds(10, 110, 778, 232);
		getContentPane().add(rxMessageTableScroll);
		
		rxMessageTable = new JTable();
		rxMessageTable.setRowHeight(25);
		rxMessageTable.setAutoCreateRowSorter(false);
		rxMessageTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		rxMessageTable.setModel(new DefaultTableModel(
			null,
			new String[] {
				"Sender", "Length", "Receiver", "Data", "ASCII"
			}
		));
		rxMessageTable.getColumnModel().getColumn(0).setResizable(false);
		rxMessageTable.getColumnModel().getColumn(0).setMinWidth(75);
		rxMessageTable.getColumnModel().getColumn(1).setResizable(false);
		rxMessageTable.getColumnModel().getColumn(1).setMinWidth(75);
		rxMessageTable.getColumnModel().getColumn(2).setResizable(false);
		rxMessageTable.getColumnModel().getColumn(2).setMinWidth(75);
		rxMessageTable.getColumnModel().getColumn(3).setResizable(false);
		rxMessageTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		rxMessageTable.getColumnModel().getColumn(3).setMinWidth(150);
		rxMessageTable.getColumnModel().getColumn(4).setResizable(false);
		rxMessageTable.getColumnModel().getColumn(4).setPreferredWidth(205);
		rxMessageTable.getColumnModel().getColumn(4).setMinWidth(200);
		rxMessageTable.setCellSelectionEnabled(true);
		rxMessageTable.setColumnSelectionAllowed(false);
		rxMessageTable.setRowSelectionAllowed(true);
		rxMessageTable.setBounds(303, 349, 485, 239);
		rxMessageTableScroll.setViewportView(rxMessageTable);
		
		this.rxMessageTableScroll = rxMessageTableScroll;
		
		Class<?> col_class = rxMessageTable.getColumnClass(0);
		rxMessageTable.setDefaultEditor(col_class, null);
		
		JPanel txControlPanel = new JPanel();
		txControlPanel.setBounds(0, 354, 800, 246);
		getContentPane().add(txControlPanel);
		txControlPanel.setLayout(null);
		
		JScrollPane txMessageTableScroll = new JScrollPane();
		txMessageTableScroll.setEnabled(true);
		txMessageTableScroll.setBounds(12, 12, 776, 102);
		txControlPanel.add(txMessageTableScroll);
		
		txMessageTable = new JTable();
		txMessageTable.setToolTipText("The table of AIBus messages to send.");
		txMessageTable.setBounds(303, 349, 485, 239);
		txMessageTable.setRowHeight(25);
		txMessageTable.setAutoCreateRowSorter(false);
		txMessageTable.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		txMessageTable.setModel(new DefaultTableModel(
			null,
			new String[] {
				"Sender", "Length", "Receiver", "Data", "ASCII"
			}
		));
		txMessageTable.getColumnModel().getColumn(0).setResizable(false);
		txMessageTable.getColumnModel().getColumn(0).setMinWidth(75);
		txMessageTable.getColumnModel().getColumn(1).setResizable(false);
		txMessageTable.getColumnModel().getColumn(1).setMinWidth(75);
		txMessageTable.getColumnModel().getColumn(2).setResizable(false);
		txMessageTable.getColumnModel().getColumn(2).setMinWidth(75);
		txMessageTable.getColumnModel().getColumn(3).setResizable(false);
		txMessageTable.getColumnModel().getColumn(3).setPreferredWidth(150);
		txMessageTable.getColumnModel().getColumn(3).setMinWidth(150);
		txMessageTable.getColumnModel().getColumn(4).setResizable(false);
		txMessageTable.getColumnModel().getColumn(4).setPreferredWidth(205);
		txMessageTable.getColumnModel().getColumn(4).setMinWidth(200);
		txMessageTable.setCellSelectionEnabled(true);
		txMessageTable.setColumnSelectionAllowed(false);
		txMessageTable.setRowSelectionAllowed(true);
		txMessageTableScroll.setViewportView(txMessageTable);

		col_class = txMessageTable.getColumnClass(0);
		txMessageTable.setDefaultEditor(col_class, null);
		
		JButton btnNewMessage = new JButton("New");
		btnNewMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				new EditAIBusWindow(self, self.controller.getCommunicator());
			}
		});
		btnNewMessage.setToolTipText("Create a new AIBus message to send.");
		btnNewMessage.setBounds(12, 126, 117, 27);
		txControlPanel.add(btnNewMessage);
		
		JButton btnSendSelected = new JButton("Send Selected");
		btnSendSelected.setToolTipText("Send the selected AIBus message.");
		btnSendSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final int msg_index = txMessageTable.getSelectedRow();
				handler.sendAIBusMessage(msg_index);
			}
		});
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.setToolTipText("Remove the selected AIBus message.");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final int msg_index = txMessageTable.getSelectedRow();
				handler.removeAIBusMessageTx(msg_index);
			}
		});
		btnDelete.setBounds(12, 165, 117, 27);
		txControlPanel.add(btnDelete);
		
		JButton btnModify = new JButton("Modify");
		btnModify.setToolTipText("Edit the selected AIBus message.");
		btnModify.setBounds(12, 204, 117, 27);
		btnModify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				final int msg_index = txMessageTable.getSelectedRow();

				if(msg_index < 0)
					return;

				AIData msg = self.controller.getCommunicator().getTxMessageList() [msg_index];
				new EditAIBusWindow(self, self.controller.getCommunicator(), msg, msg_index);
			}
		});
		txControlPanel.add(btnModify);

		btnSendSelected.setBounds(140, 126, 136, 27);
		txControlPanel.add(btnSendSelected);
		
		JButton btnSaveTxList = new JButton("Save TX List");
		btnSaveTxList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				controller.activateTxListSave();
			}
		});
		btnSaveTxList.setBounds(140, 165, 136, 27);
		txControlPanel.add(btnSaveTxList);
		
		JButton btnLoadTxList = new JButton("Load TX List");
		btnLoadTxList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(txMessageTable.getRowCount() > 0) {
					final int answer = JOptionPane.showConfirmDialog(self, "This will erase all messages created in the Tx list. Proceed?", "Load", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(answer == JOptionPane.NO_OPTION)
						return;
				}

				controller.activateTxListLoad();
			}
		});
		btnLoadTxList.setBounds(140, 204, 136, 27);
		txControlPanel.add(btnLoadTxList);
		
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(self.controller.getActivePort() != null) {
					byte standard = 0;
					if(rdbtnAibusn.isSelected())
						standard = standard_aibus;
					else if(rdbtnIbus.isSelected())
						standard = standard_ibus;

					connectOrDisconnect(btnConnect, !self.controller.getActivePort().isOpen(), standard);
				}
			}
		});

		JButton btnEmulateScreen = new JButton("Screen Emulator");
		btnEmulateScreen.setToolTipText("Activate the screen emulator for quick button messages.");
		btnEmulateScreen.setBounds(600, 204, 180, 27);
		txControlPanel.add(btnEmulateScreen);
		btnEmulateScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if(self.sub_window != null)
					self.sub_window.dispatchEvent(new WindowEvent(sub_window, WindowEvent.WINDOW_CLOSING));

				self.sub_window = new ScreenEmulatorWindow(self, self.controller.getCommunicator(), rdbtnAibusn.isSelected());
			}
		});
		
		btnClearTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				DefaultTableModel rx_model = (DefaultTableModel) rxMessageTable.getModel();
				while(rx_model.getRowCount() > 0)
					rx_model.removeRow(rx_model.getRowCount() - 1);
			}
		});
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				self.controller.endProgram();
				e.getWindow().dispose();
			}
		});
		
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				serialControlPanel.setBounds(0, 0, self.getContentPane().getWidth(), 98);
				txControlPanel.setBounds(txControlPanel.getX(),txControlPanel.getY(),self.getContentPane().getWidth(), txControlPanel.getHeight());
				
				rxMessageTableScroll.setBounds(rxMessageTableScroll.getX(), rxMessageTableScroll.getY(), self.getContentPane().getWidth() - message_table_from_edge, rxMessageTableScroll.getHeight());
				txMessageTableScroll.setBounds(txMessageTableScroll.getX(), txMessageTableScroll.getY(), self.getContentPane().getWidth() - message_table_from_edge, txMessageTableScroll.getHeight());
				
				rxMessageTable.revalidate();
				rxMessageTable.repaint();
				
				txMessageTable.revalidate();
				txMessageTable.repaint();
			}
		});
		
		this.setVisible(true);
	}
	
	//Add an RX message to the table.
	public void addRxMessageToWindow(AIData message) {
		while(System.currentTimeMillis() - this.last_frame_timer < controller.getRefreshRate());

		JScrollBar vertical_bar = rxMessageTableScroll.getVerticalScrollBar();

		AIBusHandler.addAIMessageToTable(rxMessageTable, message);
		
		final int new_max = vertical_bar.getMaximum();

		if(autoscroll)
			vertical_bar.setValue(new_max + rxMessageTable.getRowHeight());

		this.last_frame_timer = System.currentTimeMillis();
	}
	
	//Add a TX message to the table.
	public void addTxMessageToWindow(AIData message) {
		AIBusHandler.addAIMessageToTable(txMessageTable, message);
	}

	//Change an existing TX message.
	public void setTxMessage(AIData message, final int row) {
		AIBusHandler.setTableAIMessage(txMessageTable, message, row);
	}
	
	//Remove a TX message from the table.
	public void removeTxMessage(final int index) {
		DefaultTableModel tx_model = (DefaultTableModel) txMessageTable.getModel();
		tx_model.removeRow(index);
	}

	//Clear the TX table.
	public void clearTxTable() {
		DefaultTableModel tx_model = (DefaultTableModel) txMessageTable.getModel();
		tx_model.setRowCount(0);
	}

	//Connect or disconnect the serial port.
	private void connectOrDisconnect(JButton button, final boolean connect, final byte standard) {
		if(this.controller.getActivePort() != null) {
			if(connect) {
				if(standard == standard_aibus) {
					this.controller.getActivePort().setBaudRate(115200);
					this.controller.getActivePort().setParity(SerialPort.NO_PARITY);
				} else if(standard == standard_ibus) {
					this.controller.getActivePort().setBaudRate(9600);
					this.controller.getActivePort().setParity(SerialPort.EVEN_PARITY);
				} else
					return;

				final boolean success = this.controller.getActivePort().openPort();
				if(!success)
					return;
				
				this.controller.getCommunicator().activatePort(this.controller.getActivePort());
				button.setText(str_disconnect);
				setBaudButtonState(true);
			} else {
				final boolean success = this.controller.getActivePort().closePort();
				this.controller.getCommunicator().deactivatePort(this.controller.getActivePort());
				
				if(!success)
					return;
				
				button.setText(str_connect);
				setBaudButtonState(false);

				if(sub_window != null)
					sub_window.dispatchEvent(new WindowEvent(sub_window, WindowEvent.WINDOW_CLOSING));
			}
		}
	}

	//Set the baud control state.
	private void setBaudButtonState(final boolean lock) {
		Enumeration<AbstractButton> buttons = busStandardGroup.getElements();
		boolean complete = false;
		while(!complete) {
			try {
				AbstractButton new_button = buttons.nextElement();
				new_button.setEnabled(!lock);
			} catch(NoSuchElementException e) {
				complete = true;
				break;
			}
		}
	}

	//Refresh the serial list.
	private void serialComboBoxRefresh(JComboBox<String> combo_box, JButton connect_button) {
		connectOrDisconnect(connect_button, false, (byte) 0);

		SerialPort[] ports = this.controller.getAllPorts();
		String[] names = new String[ports.length];
		
		for(int i=0;i<ports.length;i+=1)
			names[i] = ports[i].getSystemPortName();
		
		final int ind = combo_box.getSelectedIndex();
		String sel = "";
		if(ind >= 0)
			sel = (String) combo_box.getSelectedItem();
		
		combo_box.removeAllItems();
		
		for(int i=0;i<names.length;i+=1)
			combo_box.addItem(names[i]);
		
		if(ind < 0)
			combo_box.setSelectedIndex(-1);
		else {
			for(int i=0;i<ports.length;i+=1) {
				if(ports[i].getSystemPortName().equals(sel)) {
					combo_box.setSelectedIndex(i);
					break;
				}
			}
		}
		
		if(this.controller.getActivePort() == null) {
			connect_button.setText(str_connect);
			connect_button.setEnabled(false);
		} else {
			int index = this.controller.getPortIndex(this.controller.getActivePort());
			if(index >= 0 && index < combo_box.getItemCount()) {
				combo_box.setSelectedIndex(index);
				connect_button.setEnabled(true);
				if(this.controller.getActivePort().isOpen())
					connect_button.setText(str_disconnect);
				else
					connect_button.setText(str_connect);
			} else {
				combo_box.setSelectedIndex(-1);
				connect_button.setEnabled(false);
				connect_button.setText(str_connect);
			}
		}
	}
}
