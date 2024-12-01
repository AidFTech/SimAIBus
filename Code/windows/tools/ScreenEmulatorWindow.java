package tools;

import java.awt.Dimension;

import javax.swing.JFrame;

import aibus.AIBusHandler;
import aibus.AIData;
import aibus.ScreenReceiverGroup;
import main_window.SimAIBusMainWindow;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ScreenEmulatorWindow extends JFrame {

	private static final long serialVersionUID = 8309381299891872211L;
	private static final int w_width = 640, w_height = 480;

	private static final byte AI_BUTTON_HOME = 0x20, AI_BUTTON_SOURCE = 0x23, AI_BUTTON_AUDIO = 0x26, AI_BUTTON_SKIP_UP = 0x25, AI_BUTTON_SKIP_DN = 0x24, AI_BUTTON_VOL = 0x6, AI_BUTTON_FMAM = 0x36, AI_BUTTON_MEDIA = 0x38, AI_BUTTON_AUX = 0x34, AI_BUTTON_SAT = 0x35, AI_BUTTON_PHONE = 0x50, AI_BUTTON_MENU = 0x51, AI_BUTTON_TONE = 0x52, AI_BUTTON_INFO = 0x53, AI_BUTTON_CLOCK = 0x54, AI_BUTTON_BACK = 0x27, AI_BUTTON_UP = 0x28, AI_BUTTON_DOWN = 0x29, AI_BUTTON_LEFT = 0x2A, AI_BUTTON_RIGHT = 0x2B, AI_BUTTON_ENTER = 0x7;
	private static final byte AI_BUTTON_P1 = 0x11, AI_BUTTON_P2 = 0x12, AI_BUTTON_P3 = 0x13, AI_BUTTON_P4 = 0x14, AI_BUTTON_P5 = 0x15, AI_BUTTON_P6 = 0x16;

	private static final byte IB_BUTTON_INFO = 0x22, IB_BUTTON_FM = 0x31, IB_BUTTON_AM = 0x21, IB_BUTTON_MODE = 0x23, IB_BUTTON_AUDIO = 0x30, IB_BUTTON_PHONE = 0x8, IB_BUTTON_DIR = 0x14, IB_BUTTON_CLOCK = 0x7, IB_BUTTON_TONE = 0x4, IB_BUTTON_SELECT = 0x20, IB_BUTTON_SKIP_DN = 0x10, IB_BUTTON_SKIP_UP = 0x0, IB_BUTTON_MENU = 0x34, IB_BUTTON_ENTER = 0x5, IB_BUTTON_VOL = 0x6;
	private static final byte IB_BUTTON_P1 = 0x11, IB_BUTTON_P2 = 0x1, IB_BUTTON_P3 = 0x12, IB_BUTTON_P4 = 0x2, IB_BUTTON_P5 = 0x13, IB_BUTTON_P6 = 0x3;
	
	private static final int button_cluster_start = 70;

	private AIBusHandler handler;
	private ScreenReceiverGroup receiver_group;
	
	public ScreenEmulatorWindow(SimAIBusMainWindow parent, AIBusHandler handler, final boolean aibus_full) {
		this.handler = handler;
		this.receiver_group = this.handler.getReceiverGroup();

		if(aibus_full)
			this.receiver_group.screen_emulator_on = true;

		this.setTitle("Screen Emulator");
		this.setFocusable(true);
		this.setResizable(false);

		this.getContentPane().setPreferredSize(new Dimension(w_width, w_height));
		this.getContentPane().setMinimumSize(new Dimension(w_width, w_height));
		this.getContentPane().setSize(this.getContentPane().getPreferredSize());
		this.pack();

		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.setLocationRelativeTo(parent);
		this.getContentPane().setLayout(null);

		ScreenEmulatorWindow self = this;
		
		if(aibus_full) { //AIBus buttons.
			JButton btnHome = new JButton("Home");
			btnHome.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_HOME);
				}
			});
			btnHome.setBounds(12, 12, 117, 25);
			getContentPane().add(btnHome);
			
			JButton btnSource = new JButton("Source");
			btnSource.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_SOURCE);
				}
			});
			btnSource.setBounds(12, button_cluster_start, 117, 25);
			getContentPane().add(btnSource);
			
			JButton btnFMAM = new JButton("FM/AM");
			btnFMAM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_FMAM);
				}
			});
			btnFMAM.setBounds(12, button_cluster_start + 25*1, 117, 25);
			getContentPane().add(btnFMAM);
			
			JButton btnTapeCD = new JButton("Tape/CD");
			btnTapeCD.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_MEDIA);
				}
			});
			btnTapeCD.setBounds(12, button_cluster_start + 25*2, 117, 25);
			getContentPane().add(btnTapeCD);
			
			JButton btnAux = new JButton("Aux");
			btnAux.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_AUX);
				}
			});
			btnAux.setBounds(12, button_cluster_start + 25*3, 117, 25);
			getContentPane().add(btnAux);
			
			JButton btnXM = new JButton("XM");
			btnXM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_SAT);
				}
			});
			btnXM.setBounds(12, button_cluster_start + 25*4, 117, 25);
			getContentPane().add(btnXM);
			
			JButton btnAudio = new JButton("Audio");
			btnAudio.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_AUDIO);
				}
			});
			btnAudio.setBounds(12, 209, 117, 25);
			getContentPane().add(btnAudio);
			
			JButton btnSkipUp = new JButton("Skip +");
			btnSkipUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_SKIP_UP);
				}
			});
			btnSkipUp.setBounds(12, 263, 117, 25);
			getContentPane().add(btnSkipUp);
			
			JButton btnSkipDn = new JButton("Skip -");
			btnSkipDn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_SKIP_DN);
				}
			});
			btnSkipDn.setBounds(12, 289, 117, 25);
			getContentPane().add(btnSkipDn);
			
			JButton btnVolPush = new JButton("Vol. Push");
			btnVolPush.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_VOL);
				}
			});
			btnVolPush.setBounds(12, 386, 117, 25);
			getContentPane().add(btnVolPush);
			
			JButton btnVolumeUp = new JButton("Volume +");
			btnVolumeUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiRotateKnob(AI_BUTTON_VOL, (byte)1, true);
				}
			});
			btnVolumeUp.setBounds(12, 418, 117, 25);
			getContentPane().add(btnVolumeUp);
			
			JButton btnVolumeDn = new JButton("Volume -");
			btnVolumeDn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiRotateKnob(AI_BUTTON_VOL, (byte)1, false);
				}
			});
			btnVolumeDn.setBounds(12, 443, 117, 25);
			getContentPane().add(btnVolumeDn);
			
			JButton btnPhone = new JButton("Phone");
			btnPhone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_PHONE);
				}
			});
			btnPhone.setBounds(511, 23, 117, 25);
			getContentPane().add(btnPhone);
			
			JButton btnMenu = new JButton("Menu");
			btnMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_MENU);
				}
			});
			btnMenu.setBounds(511, button_cluster_start, 117, 25);
			getContentPane().add(btnMenu);
			
			JButton btnTone = new JButton("Tone");
			btnTone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_TONE);
				}
			});
			btnTone.setBounds(511, button_cluster_start + 25*1, 117, 25);
			getContentPane().add(btnTone);
			
			JButton btnInfo = new JButton("Info");
			btnInfo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_INFO);
				}
			});
			btnInfo.setBounds(511, button_cluster_start + 25*2, 117, 25);
			getContentPane().add(btnInfo);
			
			JButton btnClock = new JButton("Clock");
			btnClock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_CLOCK);
				}
			});
			btnClock.setBounds(511, button_cluster_start + 25*3, 117, 25);
			getContentPane().add(btnClock);
			
			JButton btnBack = new JButton("Back");
			btnBack.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_BACK);
				}
			});
			btnBack.setBounds(511, 183, 117, 25);
			getContentPane().add(btnBack);
			
			JButton btnToggleCW = new JButton("Toggle +");
			btnToggleCW.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiRotateKnob(AI_BUTTON_ENTER, (byte)1, true);
				}
			});
			btnToggleCW.setBounds(460, 325, 117, 25);
			getContentPane().add(btnToggleCW);
			
			JButton btnTogPush = new JButton("Tog. Push");
			btnTogPush.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_ENTER);
				}
			});
			btnTogPush.setBounds(460, 350, 117, 25);
			getContentPane().add(btnTogPush);
			
			JButton btnToggleCO = new JButton("Toggle -");
			btnToggleCO.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiRotateKnob(AI_BUTTON_ENTER, (byte)1, false);
				}
			});
			btnToggleCO.setBounds(460, 375, 117, 25);
			getContentPane().add(btnToggleCO);
			
			JButton btnUp = new JButton("\u25B2");
			btnUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_UP);
				}
			});
			btnUp.setBounds(490, 298, 50, 25);
			getContentPane().add(btnUp);
			
			JButton btnDn = new JButton("\u25BC");
			btnDn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_DOWN);
				}
			});
			btnDn.setBounds(490, 402, 50, 25);
			getContentPane().add(btnDn);
			
			JButton btnLeft = new JButton("\u25C0");
			btnLeft.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_LEFT);
				}
			});
			btnLeft.setBounds(408, 350, 50, 25);
			getContentPane().add(btnLeft);

			JButton btnRight = new JButton("\u25B6");
			btnRight.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_RIGHT);
				}
			});
			btnRight.setBounds(580, 350, 50, 25);
			getContentPane().add(btnRight);
			
			JButton btnP1 = new JButton("1");
			btnP1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_P1);
				}
			});
			btnP1.setBounds(145, 443, 50, 25);
			getContentPane().add(btnP1);

			JButton btnP2 = new JButton("2");
			btnP2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_P2);
				}
			});
			btnP2.setBounds(195, 443, 50, 25);
			getContentPane().add(btnP2);

			JButton btnP3 = new JButton("3");
			btnP3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_P3);
				}
			});
			btnP3.setBounds(245, 443, 50, 25);
			getContentPane().add(btnP3);

			JButton btnP4 = new JButton("4");
			btnP4.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_P4);
				}
			});
			btnP4.setBounds(295, 443, 50, 25);
			getContentPane().add(btnP4);

			JButton btnP5 = new JButton("5");
			btnP5.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_P5);
				}
			});
			btnP5.setBounds(345, 443, 50, 25);
			getContentPane().add(btnP5);

			JButton btnP6 = new JButton("6");
			btnP6.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					aiButtonPress(AI_BUTTON_P6);
				}
			});
			btnP6.setBounds(395, 443, 50, 25);
			getContentPane().add(btnP6);
		} else {
			JButton btnInfo = new JButton("Info");
			btnInfo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_INFO);
				}
			});
			btnInfo.setBounds(12, button_cluster_start, 117, 25);
			getContentPane().add(btnInfo);

			JButton btnP1 = new JButton("1");
			btnP1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_P1);
				}
			});
			btnP1.setBounds(12, button_cluster_start + 25*1, 68, 25);
			getContentPane().add(btnP1);

			JButton btnP2 = new JButton("2");
			btnP2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_P2);
				}
			});
			btnP2.setBounds(12+68, button_cluster_start + 25*1, 68, 25);
			getContentPane().add(btnP2);

			JButton btnP3 = new JButton("3");
			btnP3.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_P3);
				}
			});
			btnP3.setBounds(12, button_cluster_start + 25*2, 68, 25);
			getContentPane().add(btnP3);

			JButton btnP4 = new JButton("4");
			btnP4.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_P4);
				}
			});
			btnP4.setBounds(12+68, button_cluster_start + 25*2, 68, 25);
			getContentPane().add(btnP4);

			JButton btnP5 = new JButton("5");
			btnP5.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_P5);
				}
			});
			btnP5.setBounds(12, button_cluster_start + 25*3, 68, 25);
			getContentPane().add(btnP5);

			JButton btnP6 = new JButton("6");
			btnP6.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_P6);
				}
			});
			btnP6.setBounds(12+68, button_cluster_start + 25*3, 68, 25);
			getContentPane().add(btnP6);

			JButton btnFM = new JButton("FM");
			btnFM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_FM);
				}
			});
			btnFM.setBounds(12, button_cluster_start + 10 + 25*4, 117, 25);
			getContentPane().add(btnFM);

			JButton btnAM = new JButton("AM");
			btnAM.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_AM);
				}
			});
			btnAM.setBounds(12, button_cluster_start + 10 + 25*5, 117, 25);
			getContentPane().add(btnAM);

			JButton btnMode = new JButton("Source");
			btnMode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_MODE);
				}
			});
			btnMode.setBounds(12, button_cluster_start + 10 + 25*6, 117, 25);
			getContentPane().add(btnMode);

			JButton btnAudio = new JButton("Audio");
			btnAudio.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_AUDIO);
				}
			});
			btnAudio.setBounds(12, button_cluster_start + 10 + 25*7, 117, 25);
			getContentPane().add(btnAudio);

			JButton btnVolPush = new JButton("Vol. Push");
			btnVolPush.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_VOL);
				}
			});
			btnVolPush.setBounds(12, 386, 117, 25);
			getContentPane().add(btnVolPush);

			JButton btnVolumeUp = new JButton("Volume +");
			btnVolumeUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibRotateVolumeKnob((byte) 1, true);
				}
			});
			btnVolumeUp.setBounds(12, 418, 117, 25);
			getContentPane().add(btnVolumeUp);
			
			JButton btnVolumeDn = new JButton("Volume -");
			btnVolumeDn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibRotateVolumeKnob((byte) 1, false);
				}
			});
			btnVolumeDn.setBounds(12, 443, 117, 25);
			getContentPane().add(btnVolumeDn);

			JButton btnPhone = new JButton("Phone");
			btnPhone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_PHONE);
				}
			});
			btnPhone.setBounds(511, button_cluster_start, 117, 25);
			getContentPane().add(btnPhone);

			JButton btnDir = new JButton("\u25C0  \u25B6");
			btnDir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_DIR);
				}
			});
			btnDir.setBounds(511, button_cluster_start + 25*1, 117, 25);
			getContentPane().add(btnDir);

			JButton btnClock = new JButton("Clock");
			btnClock.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_CLOCK);
				}
			});
			btnClock.setBounds(511, button_cluster_start + 25*2, 117, 25);
			getContentPane().add(btnClock);

			JButton btnTone = new JButton("Tone");
			btnTone.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_TONE);
				}
			});
			btnTone.setBounds(511, button_cluster_start + 25*3, 117, 25);
			getContentPane().add(btnTone);

			JButton btnSelect = new JButton("Select");
			btnSelect.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_SELECT);
				}
			});
			btnSelect.setBounds(511, button_cluster_start + 25*4, 117, 25);
			getContentPane().add(btnSelect);

			JButton btnSkipUp = new JButton("Skip +");
			btnSkipUp.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_SKIP_UP);
				}
			});
			btnSkipUp.setBounds(511, button_cluster_start + 10 + 25*5, 117, 25);
			getContentPane().add(btnSkipUp);

			JButton btnSkipDn = new JButton("Skip -");
			btnSkipDn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_SKIP_DN);
				}
			});
			btnSkipDn.setBounds(511, button_cluster_start + 10 + 25*6, 117, 25);
			getContentPane().add(btnSkipDn);

			JButton btnMenu = new JButton("Menu");
			btnMenu.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_MENU);
				}
			});
			btnMenu.setBounds(511, button_cluster_start + 20 + 25*7, 117, 25);
			getContentPane().add(btnMenu);

			JButton btnToggleCW = new JButton("Toggle +");
			btnToggleCW.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibRotateKnob((byte)1, true);
				}
			});
			btnToggleCW.setBounds(511, 418, 117, 25);
			getContentPane().add(btnToggleCW);
			
			JButton btnTogPush = new JButton("Tog. Push");
			btnTogPush.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibButtonPress(IB_BUTTON_ENTER);
				}
			});
			btnTogPush.setBounds(511, 386, 117, 25);
			getContentPane().add(btnTogPush);
			
			JButton btnToggleCO = new JButton("Toggle -");
			btnToggleCO.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					ibRotateKnob((byte)1, false);
				}
			});
			btnToggleCO.setBounds(511, 443, 117, 25);
			getContentPane().add(btnToggleCO);
		}

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
    		public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				self.receiver_group.screen_emulator_on = false;
				self.dispose();
			}
		});

		this.setVisible(true);
	}

	private void aiButtonPress(final byte button) {
		byte receiver = this.receiver_group.main_receiver;

		switch(button) {
			case AI_BUTTON_P1:
			case AI_BUTTON_P2:
			case AI_BUTTON_P3:
			case AI_BUTTON_P4:
			case AI_BUTTON_P5:
			case AI_BUTTON_P6:
			case AI_BUTTON_INFO:
			case AI_BUTTON_SKIP_UP:
			case AI_BUTTON_SKIP_DN:
				receiver = this.receiver_group.source_receiver;
				break;
			case AI_BUTTON_AUX:
			case AI_BUTTON_FMAM:
			case AI_BUTTON_MEDIA:
			case AI_BUTTON_SAT:
			case AI_BUTTON_SOURCE:
			case AI_BUTTON_TONE:
			case AI_BUTTON_VOL:
				receiver = this.receiver_group.audio_receiver;
				break;
		}

		byte[] press_data = {0x30, button, 0x0};
		AIData press_msg = new AIData((byte)press_data.length, (byte)0x7, receiver);
		press_msg.refreshAIData(press_data);

		this.handler.sendAIBusMessage(press_msg);

		//TODO: Proper acknowledgement.
		final long wait_start = System.nanoTime()/1000000;
		while(System.nanoTime()/1000000 - wait_start < 100);

		byte[] release_data = {0x30, button, (byte)0x80};
		AIData release_msg = new AIData((byte)release_data.length, (byte)0x7, receiver);
		release_msg.refreshAIData(release_data);

		this.handler.sendAIBusMessage(release_msg);
	}

	private void aiRotateKnob(final byte knob, final byte steps, final boolean clockwise) {
		byte receiver = this.receiver_group.main_receiver;

		switch(knob) {
			case AI_BUTTON_VOL:
				receiver = this.receiver_group.audio_receiver;
				break;
		}

		byte[] rotate_data = {0x32, knob, (byte) (steps&0xF)};
		if(clockwise)
			rotate_data[2] |= 0x10;
		
		AIData rotate_msg = new AIData((byte)rotate_data.length, (byte)0x7, receiver);
		rotate_msg.refreshAIData(rotate_data);

		this.handler.sendAIBusMessage(rotate_msg);
	}

	private void ibButtonPress(final byte button) {
		byte receiver = 0x3B;

		switch(button) {
			case IB_BUTTON_AM:
			case IB_BUTTON_AUDIO:
			case IB_BUTTON_DIR:
			case IB_BUTTON_FM:
			case IB_BUTTON_INFO:
			case IB_BUTTON_MODE:
			case IB_BUTTON_P1:
			case IB_BUTTON_P2:
			case IB_BUTTON_P3:
			case IB_BUTTON_P4:
			case IB_BUTTON_P5:
			case IB_BUTTON_P6:
			case IB_BUTTON_SKIP_DN:
			case IB_BUTTON_SKIP_UP:
			case IB_BUTTON_TONE:
			case IB_BUTTON_VOL:
				receiver = 0x68;
				break;
		}

		byte[] press_data = {0x48, button};
		AIData press_msg = new AIData((byte)press_data.length, (byte)0xF0, receiver);
		press_msg.refreshAIData(press_data);

		handler.sendAIBusMessage(press_msg);

		byte[] release_data = {0x48, (byte) (button|(byte)0x80)};
		AIData release_msg = new AIData((byte)release_data.length, (byte)0xF0, receiver);
		release_msg.refreshAIData(release_data);

		handler.sendAIBusMessage(release_msg);
	}

	private void ibRotateKnob(final byte steps, final boolean clockwise) {
		byte[] knob_data = {0x49, (byte)(steps&0xF)};

		if(clockwise)
			knob_data[1] |= (byte)0x80;
		
		AIData knob_msg = new AIData((byte)knob_data.length, (byte)0xF0, (byte)0x3B);
		knob_msg.refreshAIData(knob_data);

		handler.sendAIBusMessage(knob_msg);
	}

	private void ibRotateVolumeKnob(final byte steps, final boolean clockwise) {
		byte[] knob_data = {0x32, (byte)((steps&0xF)<<4)};

		if(clockwise)
			knob_data[1] |= 0x1;
		
		AIData knob_msg = new AIData((byte)knob_data.length, (byte)0xF0, (byte)0x68);
		knob_msg.refreshAIData(knob_data);

		handler.sendAIBusMessage(knob_msg);
	}
}
