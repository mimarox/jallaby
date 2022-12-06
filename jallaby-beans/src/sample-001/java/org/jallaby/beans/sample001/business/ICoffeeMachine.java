package org.jallaby.beans.sample001.business;

public interface ICoffeeMachine {
	public void switchOn();
	public void switchOff();
	public void makeCoffee(String type);
	public void stopMakingCoffee();
	public void sleep();
	public void wakeUp();
	public void openCoffeeTray();
	public void closeCoffeeTray();
	public void openMilkTray();
	public void closeMilkTray();
	public void openSugarTray();
	public void closeSugarTray();
	public void openWaterTray();
	public void closeWaterTray();
	public boolean hasPower();
}
