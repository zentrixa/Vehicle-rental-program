public class Car extends Vehicle{
    private int numberOfSeats;


    public Car(int numberOfSeats) {
        super();
        this.numberOfSeats = numberOfSeats;
        setType("Car");
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    @Override
    public void displayDetails() {
        super.displayDetails();
        System.out.print("Number Of Seats The  Car has: " + numberOfSeats);

    }

    @Override
    public double calculateRentalRate() {
        return super.calculateRentalRate();

    }


}


