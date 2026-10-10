public class MotorCycle extends Vehicle{
    private double engineCapacity;

    public MotorCycle(double engineCapacity) {
        super();
        this.engineCapacity = engineCapacity;
        setType("Motorcycle");
    }

    public double getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(double engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.print("Engine Capacity of the Motorcycle: " + engineCapacity);
    }

    @Override
    public double calculateRentalRate() {
        double rentalRate = super.calculateRentalRate();
        return rentalRate - (rentalRate * 50.0 /100);
    }


}
