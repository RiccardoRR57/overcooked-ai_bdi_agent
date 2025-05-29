# Overcooked-AI BDI Agent

This project integrates a BDI (Belief-Desire-Intention) agent using the Jason framework with the Overcooked-AI environment.

## Requirements

- [Docker](https://www.docker.com/)  
    Used to containerize and run the application.

- [Jason BDI](https://jason.sourceforge.net/)  
    Multi-agent system framework for implementing BDI agents.

## Getting Started

1. Install Docker on your system.
2. Download and set up the Jason BDI framework.
3. Follow project-specific instructions to build and run the agent.
  
## Installation Details

### Installing Docker

- **Linux:**  
  Follow the [official Docker installation guide for Linux](https://docs.docker.com/engine/install/).
  ```sh
  sudo apt-get update
  sudo apt-get install docker-ce docker-ce-cli containerd.io
  ```
- **Windows/Mac:**  
  Download and install [Docker Desktop](https://www.docker.com/products/docker-desktop/).

After installation, verify Docker is working:
```sh
docker --version
```

### Installing Jason BDI

1. Download the Jason binary release:
   ```sh
   wget https://github.com/jason-lang/jason/releases/latest/download/jason-bin-3.2.0.zip
   ```
   
2. Extract the archive:
   ```sh
   unzip jason-bin-3.2.0.zip
   cd jason-3.2.0
   ```

3. Make the jason executable (if needed):
   ```sh
   chmod +x bin/jason
   ```

4. Add Jason to your PATH:
   ```sh
   echo 'export PATH=$PATH:'"$(pwd)/bin" >> ~/.bashrc
   source ~/.bashrc
   ```

5. Verify installation:
   ```sh
   jason --version
   ```

For more details, refer to the [Jason documentation](https://jason.sourceforge.net/wp/documentation/).

## Running the Project

1. Launch the Overcooked server:
   ```sh
   cd server_overcooked
   ./up.sh
   ```

2. Configure the environment in `bdi_agent/build.gradle`:
   ```groovy
   // Edit the args line in the run task:
   args = ['bdi_agent.mas2j']           // Basic environment
   args = ['cramped_room.mas2j']        // Cramped room layout
   args = ['forced_coordination.mas2j'] // Forced coordination layout
   args = ['marshmellow_experiment.mas2j'] // Marshmallow experiment
   ```

3. Run the Jason BDI agent:
   ```sh
   cd bdi_agent
   jason file.mas2j
   ```