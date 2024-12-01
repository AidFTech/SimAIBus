package aibus;

public class ScreenReceiverGroup {
	public byte main_receiver = 0x1, audio_receiver = 0x1, source_receiver = 0x1;
	public boolean main_receiver_set = false, audio_receiver_set = false, source_receiver_set = false;

	public boolean screen_emulator_on = false;
}