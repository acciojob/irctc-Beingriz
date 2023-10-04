package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");

        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
                //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
                //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object

        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db



        // Check Available Tickets
        Optional<Train> trainOptional  =  trainRepository.findById(bookTicketEntryDto.getTrainId());
        Train train = trainOptional.get();
        if(train.getNoOfSeats() <  bookTicketEntryDto.getNoOfSeats()){
            throw new Exception("Less tickets are available");
        }

        // Check Valid Stations
        String routs = train.getRoute();
        String fromStation  = bookTicketEntryDto.getFromStation().toString();
        String toStation  = bookTicketEntryDto.getToStation().toString();

        String[]  stationList =  routs.split(",");
        boolean fromStationFound = false;
        boolean toStationFound = false;
        boolean isValid = false;
        int fromindex =0, toindex = 0;
        for (String station : stationList ) {
            if(station.equals(fromStation)){
               fromStationFound  = true;
               fromindex++;
            }
            if(station.equals(toStation)){
               toStationFound  = true;
               toindex++;
            }

            if(fromStationFound && toStationFound){
                isValid = true;

            }
        }
        if(!isValid){
            throw new Exception("Invalid stations");
        }

        // Caclculate Fare
        int fare =  ((stationList.length -(fromindex-toindex)-1)*300);

        // Creating Ticket
        Ticket newTicket = new Ticket();

        //Get Passenger

        newTicket.setFromStation(bookTicketEntryDto.getFromStation());
        newTicket.setToStation(bookTicketEntryDto.getToStation());
        newTicket.setTrain(train);
        newTicket.setTotalFare(fare);

        Optional<Passenger> passengerOptional =  passengerRepository.findById(bookTicketEntryDto.getBookingPersonId());
        Passenger passenger = passengerOptional.get();
        newTicket.getPassengersList().add(passenger); // Adding in Passengers in Ticket
        List<Integer> passengerIds =  bookTicketEntryDto.getPassengerIds();
        passengerIds.add(bookTicketEntryDto.getBookingPersonId());
        passenger.getBookedTickets().add(newTicket);

        Ticket savedTicket = ticketRepository.save(newTicket); // Ticket Created

        train.getBookedTickets().add(savedTicket); // Adding Ticket in Train
        Train savedTrain = trainRepository.save(train); // Saved Train with Tice

       return savedTicket.getTicketId();

    }
}
