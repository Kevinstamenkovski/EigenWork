package dev.eigenworks.signal;

/** Closed value family prevents arbitrary mutable objects from entering signal networks. */
public sealed interface SignalValue permits BooleanSignal, IntegerSignal, ScalarSignal, DigitalWord {
}

