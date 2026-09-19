package dev.eigenworks.networking;

@FunctionalInterface
public interface SpiPeripheral { byte[] transfer(byte[] controllerData); }
