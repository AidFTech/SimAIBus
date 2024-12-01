package aibus;

public class AIData {
	public byte[] data;
	public short l;
	public byte sender, receiver;
	
	public static final long AI_BAUD = 115200;
	
	public AIData() {
		this((short)0, (byte)0, (byte)0);
	}
	
	public AIData(short newl, final byte sender, final byte receiver) {
		this.l = newl;
		this.data = new byte[newl];
		this.sender = sender;
		this.receiver = receiver;
	}
	
	public AIData(AIData copy) {
		this.l = copy.l;
		this.data = new byte[this.l];
		
		this.sender = copy.sender;
		this.receiver = copy.receiver;
		
		for(int i=0;i<this.l;i+=1)
			this.data[i] = copy.data[i];
	}
	
	public void refreshAIData(short newl) {
		this.l = newl;
		this.data = new byte[newl];
	}
	
	public void refreshAIData(AIData newl) {
		this.l = newl.l;
		this.data = new byte[this.l];

		this.sender = newl.sender;
		this.receiver = newl.receiver;
		
		for(int i=0;i<l;i+=1)
			this.data[i] = newl.data[i];
	}

	public void refreshAIData(byte[] newb) {
		this.l = (short)newb.length;
		this.data = new byte[l];

		for(int i=0;i<l;i+=1)
			this.data[i] = newb[i];
	}

	public byte[] getBytes() {
		byte[] the_return = new byte[this.l + 4];
		the_return[0] = this.sender;
		the_return[1] = (byte) (this.l + 2);
		the_return[2] = this.receiver;

		for(int i=0;i<this.data.length;i+=1)
			the_return[i+3] = this.data[i];

		the_return[the_return.length - 1] = this.getChecksum();
		return the_return;
	}
	
	public byte getChecksum() {
		return getChecksum(this);
	}

	public static byte getChecksum(AIData ai_b) {
		byte data[] = new byte[ai_b.l + 4];
		data[0] = ai_b.sender;
		data[1] = (byte)(ai_b.l + 2);
		data[2] = ai_b.receiver;
		
		for(int i=0;i<ai_b.l;i+=1)
			data[i+3] = ai_b.data[i];

		return getChecksum(data);
	}

	public static byte getChecksum(byte[] data) {
		final short l = (short)data.length;
			
		int checksum = 0;
		for(int i=0;i<l-1;i+=1)
			checksum ^= data[i];
		
		return ((byte)checksum);
	}

	public static boolean checkValidity(byte[] data) {
		final short l = (short)data.length;
		
		if(l<4)
			return false;
		if(data[1] != l-2)
			return false;

		byte checksum = 0;

		for(int i=0;i<l-1;i+=1) {
			checksum ^= data[i];
		}
		
		if(checksum == data[l-1])
			return true;
		else
			return false;
	}
	
	public static boolean checkDestination(AIData ai_b, final byte dest_id) {
		if(ai_b.receiver == dest_id || ai_b.receiver == (byte)0xFF)
			return true;
		else
			return false;
	}
}
