package worm;

public class FoodCell extends Cell {
	
	public FoodCell(int x, int y) {
		this(x, y, 1);
	}
	
	public FoodCell(int x, int y, int freshness) {
		this(x, y, freshness, 0);
	}
	
	public FoodCell(int x, int y, int freshness, int rateOfDecay) {
		super(x, y);
		mFood = new Food(freshness, rateOfDecay);
	}
	
	private Food mFood;
	
	public Food getFood() {
		return mFood;
	}
}
