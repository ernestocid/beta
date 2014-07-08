package testgeneration;

public class Block {

	
	private String block;
	private boolean isNegative;

	
	public Block(String block, boolean isNegative) {
		this.block = block;
		this.isNegative = isNegative;
	}

	

	public String getBlock() {
		return block;
	}

	

	public void setBlock(String block) {
		this.block = block;
	}

	

	public boolean isNegative() {
		return isNegative;
	}

	

	public void setNegative(boolean isNegative) {
		this.isNegative = isNegative;
	}
	
	
	
	@Override
	public String toString() {
		return this.block;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Block) {
			Block comparisonBlock = (Block) obj;
			if(this.getBlock().equals(comparisonBlock.getBlock()) && 
			   this.isNegative() == comparisonBlock.isNegative()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
