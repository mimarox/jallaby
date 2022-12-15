package org.jallaby.beans.sample001.business;

public interface CoffeeMachineInterface {
	void switchOn();
	void switchOff();
	void makeCoffee(String type);
	void stopMakingCoffee();
	void sleep();
	void wakeUp();
	void openCoffeeTray();
	void closeCoffeeTray();
	void openMilkTray();
	void closeMilkTray();
	void openSugarTray();
	void closeSugarTray();
	void openWaterTray();
	void closeWaterTray();
	boolean hasPower();
}
