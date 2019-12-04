import java.util.Arrays;

public class Move implements Comparable{
	
	private long position;
	private long flip;
	private long[] maskArr;
	int rank;
	int diskCount;
	double value;
	static boolean sortByValue;
	
	public static final Move PASSMOVE = new Move(0x0L,0x0L);
	
	public Move(long position, long flip)
	{
		this.position = position;
		this.flip = flip;
		this.maskArr = new long[64];
		long mask = 0x8000000000000000L;
		for (int i=0; i<maskArr.length; i++)
		{
			maskArr[i] = mask;
			mask = mask>>>1;
			
		}
	}
	
	public long getFlip()
	{
		return flip;
	}
	public long getPosition()
	{
		return position;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + diskCount;
		result = prime * result + (int) (flip ^ (flip >>> 32));
		result = prime * result + Arrays.hashCode(maskArr);
		result = prime * result + (int) (position ^ (position >>> 32));
		result = prime * result + rank;
		return result;
	}

	@Override
	public int compareTo(Object obj) {
		if((obj!=null)&&(getClass()==obj.getClass()))
		{
			Move other = (Move) obj;
			return (this.rank - other.rank);
		}
		else
		{
			return 0;
		}
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Move other = (Move) obj;
		if (diskCount != other.diskCount) return false;
		if (flip != other.flip) return false;
		if (!Arrays.equals(maskArr, other.maskArr)) return false;
		if (position != other.position) return false;
		if (rank != other.rank) return false;
		return true;
	}

	@Override
	public String toString()
	{
		int col = 0,row = 0;
		int index = 1;
		for(int i=1; i<=64; i++) 
		{	
			if((maskArr[i-1]&position) != 0)
			{
				col = i%8;
				row = index;
			}
			if(i%8==0)
			{
				index++;
			}
		}
		
		String column = "";
		if(col==1)
		{
			column="a";
		}
		else if(col==2)
		{
			column="b";
		}
		else if(col==3)
		{
			column="c";
		}
		else if(col==4)
		{
			column="d";
		}
		else if(col==5)
		{
			column="e";
		}
		else if(col==6)
		{
			column="f";
		}
		else if(col==7)
		{
			column="g";
		}
		else if(col==0)
		{
			column="h";
		}
		String output = " "+column+" "+row;
		return output;
	}
}
