package tools;

import java.util.ArrayList;

import java.awt.Component;

import javax.swing.JDialog;

import aibus.AIBusHandler;
import aibus.AIData;
import main_window.SimAIBusMainWindow;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;

public class EditAIBusWindow extends JDialog {

	private static final long serialVersionUID = -3121935389821791842L;
	
	private static final int default_w = 65, default_h = 40;
	private boolean split = false;

	public EditAIBusWindow(SimAIBusMainWindow parent, AIBusHandler handler) {
		this(parent, handler, null, -1);
	}
	
	public EditAIBusWindow(SimAIBusMainWindow parent, AIBusHandler handler, AIData ai_msg, final int row) {
		super(parent, true);

		final boolean populate_data = ai_msg != null;
		
		EditAIBusWindow self = this;
		
		this.setTitle("Add New AIBus Message");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(647,355);
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JTextField textFieldSender = new JTextField();
		textFieldSender.setToolTipText("Enter the desired message sender.");
		textFieldSender.setBounds(95, 12, 56, 35);
		getContentPane().add(textFieldSender);

		if(populate_data)
			textFieldSender.setText(String.format("%X", ai_msg.sender));

		textFieldSender.setColumns(2);
		
		JLabel lblSender = new JLabel("Sender");
		lblSender.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSender.setBounds(7, 12, 71, 35);
		getContentPane().add(lblSender);
		
		JTextField textFieldLength = new JTextField();
		textFieldLength.setText("8");
		textFieldLength.setToolTipText("Enter the desired number of message bytes, not including checksum, in decimal.");
		textFieldLength.setColumns(2);
		textFieldLength.setBounds(313, 12, 56, 35);
		getContentPane().add(textFieldLength);
		textFieldLength.setEnabled(self.split);
		
		JLabel lblLength = new JLabel("Length");
		lblLength.setHorizontalAlignment(SwingConstants.TRAILING);
		lblLength.setBounds(225, 12, 71, 35);
		getContentPane().add(lblLength);
		
		JTextField textFieldReceiver = new JTextField();
		textFieldReceiver.setToolTipText("Enter the desired message receiver.");
		textFieldReceiver.setColumns(2);
		textFieldReceiver.setBounds(545, 12, 56, 35);

		if(populate_data)
			textFieldReceiver.setText(String.format("%X", ai_msg.receiver));

		getContentPane().add(textFieldReceiver);
		
		JLabel lblReceiver = new JLabel("Receiver");
		lblReceiver.setHorizontalAlignment(SwingConstants.TRAILING);
		lblReceiver.setBounds(457, 12, 71, 35);
		getContentPane().add(lblReceiver);
		
		JLabel lblMessageData = new JLabel("Message Data");
		lblMessageData.setBounds(0, 55, 637, 25);
		getContentPane().add(lblMessageData);
		lblMessageData.setHorizontalAlignment(SwingConstants.CENTER);
		
		JCheckBox chckbxSplitDataFields = new JCheckBox("Split Data Fields");
		chckbxSplitDataFields.setToolTipText("Check to enter data bytes into individual fields, uncheck to use a full text field.");
		chckbxSplitDataFields.setBounds(475, 276, 150, 27);
		getContentPane().add(chckbxSplitDataFields);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBounds(10, 82, 615, 182);
		getContentPane().add(dataPanel);
		dataPanel.setLayout(null);
		
		populateDataPanel(dataPanel, Integer.valueOf(textFieldLength.getText()), self.split, ai_msg);
		
		chckbxSplitDataFields.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				self.split = chckbxSplitDataFields.isSelected();
				textFieldLength.setEnabled(self.split);
				populateDataPanel(dataPanel, Integer.valueOf(textFieldLength.getText()), self.split, ai_msg);
			}
		});
		
		JButton btnCreate = new JButton("Create");

		if(populate_data)
			btnCreate.setText("Save");

		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Component[] component_list = dataPanel.getComponents();
					if(self.split) {
						AIData new_message = new AIData(Short.valueOf(textFieldLength.getText()),
																getHexByte(textFieldSender.getText()),
																getHexByte(textFieldReceiver.getText()));
						int d = 0;
						for(int i=0;i<component_list.length;i+=1) {
							if(component_list[i] instanceof JTextField) {
								new_message.data[d] = getHexByte(((JTextField)component_list[i]).getText());
								d += 1;
							}
						}
						
						if(populate_data) {
							ai_msg.refreshAIData(new_message);
							if(row >= 0)
								parent.setTxMessage(ai_msg, row);
						} else
							handler.addAIBusMessageTx(new_message);
						
						self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));		
					} else {
						final byte sender = getHexByte(textFieldSender.getText()), receiver = getHexByte(textFieldReceiver.getText());
						if(component_list.length != 1 || !(component_list[0] instanceof JTextField)) {
							JOptionPane.showMessageDialog(self, "An internal error has occured.", "Internal Error", JOptionPane.ERROR_MESSAGE);
							return;
						}
						byte[] data = getDataString(((JTextField)component_list[0]).getText());
						AIData new_message = new AIData((short)data.length, sender, receiver);
						new_message.refreshAIData(data);

						if(populate_data) {
							ai_msg.refreshAIData(new_message);
							if(row >= 0)
								parent.setTxMessage(ai_msg, row);
						} else
							handler.addAIBusMessageTx(new_message);

						self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));	
					}
				} catch(NumberFormatException e) {
					JOptionPane.showMessageDialog(self, "A number entry is formatted incorrectly. Please check the numbers and try again.", "Number Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnCreate.setBounds(12, 276, 117, 27);
		getContentPane().add(btnCreate);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		btnCancel.setBounds(141, 276, 117, 27);
		getContentPane().add(btnCancel);
		
		textFieldLength.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				try {
				populateDataPanel(dataPanel, Integer.valueOf(textFieldLength.getText()), self.split, ai_msg);
				} catch(NumberFormatException e) {
					populateDataPanel(dataPanel, 0, self.split, ai_msg);
				}
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				try {
				populateDataPanel(dataPanel, Integer.valueOf(textFieldLength.getText()), self.split, ai_msg);
				} catch(NumberFormatException e) {
					populateDataPanel(dataPanel, 0, self.split, ai_msg);
				}
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				try {
				populateDataPanel(dataPanel, Integer.valueOf(textFieldLength.getText()), self.split, ai_msg);
				} catch(NumberFormatException e) {
					populateDataPanel(dataPanel, 0, self.split, ai_msg);
				}
			}
			
		});
		
		this.setVisible(true);
	}

	private static void populateDataPanel(JPanel panel, final int count, final boolean split, AIData ai_msg) {
		Component[] component_list = panel.getComponents();
		
		for(int i=0;i<component_list.length;i+=1) {
			if(component_list[i] instanceof JTextField)
				panel.remove(component_list[i]);
		}
		
		if(split) {
			final int buttons_per_row = 9;
			for(int i=0;i<count;i+=1) {
				
				JTextField dataField = new JTextField();
				dataField.setColumns(2);
				dataField.setBounds((i%buttons_per_row)*default_w, (i/buttons_per_row)*default_h, 56, 35);
				
				String data_tooltip = "Enter data for byte ";
				data_tooltip += Integer.valueOf(i);
				data_tooltip += ".";
				
				dataField.setToolTipText(data_tooltip);

				if(ai_msg != null && i < ai_msg.l && count == ai_msg.l)
					dataField.setText(String.format("%X", ai_msg.data[i]));
				
				panel.add(dataField);
			}
		} else {
			JTextField dataField = new JTextField();
			dataField.setBounds(0, 0, panel.getWidth(), 35);
			
			dataField.setToolTipText("Enter the desired data as a string. Use \"\" to indicate ASCII text.");

			if(ai_msg != null) {
				String data_string = "";
				for(int i=0;i<ai_msg.l;i+=1) {
					data_string += String.format("%X", ai_msg.data[i]);
					if(i < ai_msg.l - 1)
						data_string += " ";
				}

				dataField.setText(data_string);
			}

			panel.add(dataField);
		}
		
		panel.revalidate();
		panel.repaint();
	}
	
	private static byte getHexByte(String b) {
		try {
			if(!b.substring(0, 1).equals("#") && !b.substring(0, 2).toUpperCase().equals("0X"))
				b = "0x" + b;
		} catch(StringIndexOutOfBoundsException e) {
			b = "0x" + b;
		}
		
		int ab = Integer.decode(b);
		
		return (byte)ab;
	}

	private static byte[] getDataString(String data_msg) {
		ArrayList<Byte> data_al = new ArrayList<Byte>(0);

		String[] sections = data_msg.split("\"");
		for(int s=0;s<sections.length;s+=1) {
			if(s%2 == 0) {
				final int start = data_al.size();
				try {
					String[] bytes = sections[s].split(" ");
					for(int b=0;b<bytes.length;b+=1) {
						byte data = getHexByte(bytes[b]);
						data_al.add(Byte.valueOf(data));
					}
				} catch(NumberFormatException e) {
					while(data_al.size() > start)
						data_al.remove(data_al.size()-1);
					
					byte[] data = getAsciiBytes(sections[s]);
					for(int i=0;i<data.length;i+=1)
						data_al.add(Byte.valueOf(data[i]));
				}
			} else {
				byte[] data = getAsciiBytes(sections[s]);
				for(int i=0;i<data.length;i+=1)
					data_al.add(Byte.valueOf(data[i]));
			}
		}

		byte[] data = new byte[data_al.size()];
		for(int i=0;i<data.length;i+=1)
			data[i] = data_al.get(i).byteValue();

		return data;
	}

	private static byte[] getAsciiBytes(String text) {
		return text.getBytes(StandardCharsets.US_ASCII);
	}
}
