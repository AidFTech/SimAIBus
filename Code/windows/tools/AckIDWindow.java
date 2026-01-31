package tools;

import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;

import main_window.SimAIBusMainWindow;

import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;

public class AckIDWindow extends JDialog {
	private static final long serialVersionUID = -188746086582683007L;
	private ArrayList<Byte> ack_id_ptr, ack_id_list;
	private JList<String> id_jlist;

	public AckIDWindow(SimAIBusMainWindow parent, ArrayList<Byte> ack_id_ptr) {
		super(parent, true);
		this.ack_id_ptr = ack_id_ptr;

		AckIDWindow self = this;

		//Copy the list so we can edit it.
		this.ack_id_list = new ArrayList<Byte>(0);
		for(int i=0;i<this.ack_id_ptr.size();i+=1)
			ack_id_list.add(this.ack_id_ptr.get(i).byteValue());

		this.setTitle("Acknowledged IDs");
		this.setType(Type.UTILITY);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setSize(276,365);
		this.setResizable(false);
		this.setLocationRelativeTo(parent);
		getContentPane().setLayout(null);
		
		JScrollPane id_jlist_scrollpanel = new JScrollPane();
		id_jlist_scrollpanel.setBounds(12, 12, 120, 299);
		getContentPane().add(id_jlist_scrollpanel);
		
		id_jlist = new JList<>();
		id_jlist_scrollpanel.setViewportView(id_jlist);
		
		JTextField id_text_field = new JTextField();
		id_text_field.setToolTipText("Enter the desired ID.");
		id_text_field.setBounds(144, 12, 111, 30);
		getContentPane().add(id_text_field);
		id_text_field.setColumns(10);
		
		JButton button_add = new JButton("Add");
		button_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					final byte id = getHexByte(id_text_field.getText());
					if(id == 0) {
						JOptionPane.showMessageDialog(self, "AIBus ID 00 is not valid.", "Invalid Device ID", JOptionPane.ERROR_MESSAGE);
						return;
					} else if(id == (byte)0xFF) {
						JOptionPane.showMessageDialog(self, "Broadcast ID FF is not acknowledged.", "Invalid Device ID", JOptionPane.ERROR_MESSAGE);
						return;
					} else {
						for(int i=0;i<ack_id_list.size();i+=1) {
							if(ack_id_list.get(i) == id) {
								JOptionPane.showMessageDialog(self, "Device ID " + Integer.toHexString(ack_id_list.get(i)&0xFF).toUpperCase() + " is already listed.", "Invalid Device ID", JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
					}

					id_text_field.setText("");
					ack_id_list.add(Byte.valueOf(id));
					self.refreshPanelList();
				} catch (NumberFormatException err) {
					JOptionPane.showMessageDialog(self, "Invalid number format. Please check the ID number and try again.", "Number Format Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
		button_add.setToolTipText("Add the specified ID to the list.");
		button_add.setBounds(144, 54, 111, 27);
		getContentPane().add(button_add);
		
		JButton button_remove = new JButton("Remove");
		button_remove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] indices = id_jlist.getSelectedIndices();
				for(int i=0;i<indices.length;i+=1) {
					ack_id_list.remove(indices[i]);
					for(int j=i+1;j<indices.length;j+=1)
						indices[j] -= 1;
				}
				
				self.refreshPanelList();			}
		});
		button_remove.setToolTipText("Remove the selected ID from the list.");
		button_remove.setBounds(144, 93, 111, 27);
		getContentPane().add(button_remove);
		
		JButton button_ok = new JButton("OK");
		button_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.ack_id_ptr.clear();
				for(int i=0;i<ack_id_list.size();i+=1)
					self.ack_id_ptr.add(ack_id_list.get(i));

				self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		button_ok.setToolTipText("Save the changes and close the window.");
		button_ok.setBounds(144, 245, 111, 27);
		getContentPane().add(button_ok);
		
		JButton button_cancel = new JButton("Cancel");
		button_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
			}
		});
		button_cancel.setToolTipText("Close the window without saving the changes.");
		button_cancel.setBounds(144, 284, 111, 27);
		getContentPane().add(button_cancel);

		refreshPanelList();

		this.setVisible(true);
	}

	/** Refresh the panel list. */
	private void refreshPanelList() {
		DefaultListModel<String> new_model = new DefaultListModel<String>();

		for(int i=0;i<ack_id_list.size();i+=1)
			new_model.addElement(Integer.toHexString(ack_id_list.get(i)&0xFF).toUpperCase());
			
		id_jlist.setModel(new_model);
	}

	/** Get a hex byte from a string. */
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
}
