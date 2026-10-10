public class Van extends Vehicle{
    private int passengerCapacity;

    public Van(int passengerCapacity) {
        super();
        this.passengerCapacity = passengerCapacity;
        setType("Van");
    }

    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    public void setPassengerCapacity(int passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.print("Passenger Capacity The Van has: " + passengerCapacity);

    }

    @Override
    public double calculateRentalRate() {
        double rentalRate = super.calculateRentalRate();
        return rentalRate + (rentalRate * 20.0 /100);
    }
}
