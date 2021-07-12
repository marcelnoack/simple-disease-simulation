# simple-disease-simulation

A 'Game of Life' variation to simulate disease spread in a simple manner.

## Getting Started

### Local Setup

The project is an IntelliJ project. It includes a JSON library.
JavaFX and Java may need to be configured according to your system.
To be able to run the example, download JavaFX from the following [link](https://gluonhq.com/products/javafx/) and add the .jar files in the lib folder as a project library.

### params.json

You are able to adjust certain parameters in this file.

- "totalWidth": any positive integer value,
- "totalHeight": any positive integer value,
- "x0": x start coordinate of your viewport,
- "x1": x end coordinate of your viewport,
- "y0": y start coordinate of your viewport,
- "y1": y end coordinate of your viewport,
- "S": any integer that fits into the population,
- "I": any integer that fits into the population, // will be ignored if set manually in the initialDistribution array
- "R": any integer that fits into the population, // will be ignored if set manually in the initialDistribution array
- "beta": any floating point number,
- "durations": "FIXED",
- "immunityDuration": any positive integer below 32767,
- "infectionDuration": any positive integer below 32767 ,
- "maxSteps": -1 or 0 if you don't want to have maximum steps|any integer,
- "initialDistribution": [ // empty if you don't want to have fixed cells
  {"x": 1, "y": 2, "status": "SUSCEPTIBLE|INFECTED|RECOVERED"}
  ],
- "mode": "SEQUENTIAL|PARALLEL"
