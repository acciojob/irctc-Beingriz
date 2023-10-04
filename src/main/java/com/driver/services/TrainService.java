package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        List<Station> stations = trainEntryDto.getStationRoute();
        StringBuilder res =  new StringBuilder();
        for(int i=0; i<stations.size()-1; i++){
            Station st = stations.get(i);
            res.append(st.toString());
            if(i < stations.size()-1){
                res.append(",");
            }
        }
        String route  = res.toString();
        Train newTrain = new Train();
        newTrain.setRoute(route);
        newTrain.setDepartureTime(trainEntryDto.getDepartureTime());
        newTrain.setNoOfSeats(trainEntryDto.getNoOfSeats());
        Train savedTrain  =  trainRepository.save(newTrain);
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        return savedTrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){

        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
        Optional<Train> optionalTrain =  trainRepository.findById(seatAvailabilityEntryDto.getTrainId());
        if(!optionalTrain.isPresent()){
            return null;
        }
        Train foundTrain = optionalTrain.get();
       return foundTrain.getNoOfSeats();
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        Optional<Train> optionalTrain =  trainRepository.findById(trainId);
        if(!optionalTrain.isPresent()){
            return null;
        }
        Train foundTrain = optionalTrain.get();

        // Valid Station
        String[] stationList =  foundTrain.getRoute().split(",");
        boolean StationFound = false;
        for (String st : stationList ) {
            if(st.equals(station.toString())){
                StationFound  = true;
            }

        }
        if(!StationFound){
            throw new Exception("Train is not passing from this station");
        }

        List<Ticket> bookedTickets = foundTrain.getBookedTickets();
        int count = 0;
        for (Ticket tkt: bookedTickets ) {
            if(tkt.getFromStation().equals(station)) count++;
        }
        return count;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0

        Optional<Train> optionalTrain =  trainRepository.findById(trainId);
        if(!optionalTrain.isPresent()){
            return null;
        }
        Train foundTrain = optionalTrain.get();
        int maxAgePerson = Integer.MIN_VALUE;

        List<Ticket> bookedTickets = foundTrain.getBookedTickets();
        for (Ticket tkt: bookedTickets ) {
            List<Passenger> pasengers = tkt.getPassengersList();
            for (Passenger pas: pasengers )
                maxAgePerson = Math.max(pas.getAge(), maxAgePerson);

        }
        return maxAgePerson>0?maxAgePerson:0;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){

        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        List<Integer> trainId = new ArrayList<>();
        List<Train> trains  =  trainRepository.findAll();
        for (Train train: trains ) {
           String[] stations =  train.getRoute().split(",");
           for (String st : stations){
               if (st.equals(station.toString()) )
                      if( (train.getDepartureTime().equals(startTime) ||
                               (train.getDepartureTime().isAfter(startTime) && train.getDepartureTime().isBefore(endTime)) || train.getDepartureTime().equals(endTime))) {
                   trainId.add(train.getTrainId());
                   break;
               }
           }
        }
        return trainId;
    }

}
