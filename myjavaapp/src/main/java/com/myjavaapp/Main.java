package com.myjavaapp;

import java.util.Scanner;

/**
 * Main entry point for the Java YouTube Music app
 * Uses SimpMusic core modules directly!
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║  YouTube Music Java App - Core Modules Direct ║");
        System.out.println("╚════════════════════════════════════════════════╝");
        System.out.println();

        MusicApp app = new MusicApp();

        try {
            app.run();
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

