# MINIAMAZON
- **!!Note: since this is an inter-group project, and is supposed to be used together with an UPS server and a World server with the matching protocols, which are not presented in this repo. As a result, although the code presented in this repo is complete and capable by itself, the server will not pass the connection initialization stage and will not be able to present its full functionality (unless the aforementioned complementory projects are also provided.)**  
- how to run amazon server:   
    enter project root directory  
    `sudo docker-compose build`
    `sudo docker-compose up`
- how to configrue the server:
    - configure local port binding for ups server to connect:  
        Local port binding for ups is by default binded to `(host)34567:(container)1000`, in `docker-compose.yml`   
        You can change the host port to whichever you want to use, but no need to change the container port  
    - configure amazon port for clients(e.g. a browser) to connect:  
        By default this port is binded to `(host)7070:(container)7070`, in `docker-compose.yml`   
        You can change the host port to whichever you want to use, but no need to change the container port  
    - configue world server ip and port for amazon server to connect to:  
        - port:  
            In `Dockerfile` line 20: `CMD gradle run --args='1:<ups_ip> 2:<ups_port> 3:<world_ip> 4:<world_port> 5:<local_bind_ip> 6:<local_bind_port> 7:<use_UPS>'`  
            By default the target world port is set to `23456`; change `4:<world_port>` to whichever needed  
        - ip:  
            change `3:<world_ip>` to whichever needed  
    - configure whether amazon is used with a UPS server:  
        change the last parameter in `Dockerfile` line 20: `use_UPS` to:  
        - `YES`: use with a UPS server  
        - `NO`: no UPS server (in this mode, the amazon server will not wait for a ups server to connect during its start-up; however, as a result, you will not be able to purchase anything)  
- how to connect to the amazon server as a client(i.e. a browser):  
    use `your_configrued_ip:your_configured_port`(e.g. `localhost:7070`) to connect and view the website main page.
