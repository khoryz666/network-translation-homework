# Network Translation Labs

This document details the configuration steps and network concepts for two distinct laboratory scenarios involving Network Address Translation (NAT) and Port Address Translation (PAT).

---

## Scenario 1: Hosting a Remote DHCP Server with Public Routing

### Context
Company AA's network is assigned the public IP address `201.1.1.1`. It also requires connectivity to an offsite network that hosts a Remote DHCP Server, which is accessible via the public IP address `201.1.1.2`. The goal is to configure our network such that internal devices can reach the external internet (using dynamic PAT) and DHCP broadcast packets are properly forwarded to the remote server.

### Key Concepts

1. **How to assign a public address to a network in Cisco Packet Tracer?**
   First, configure the outbound router interface with the public IP address. Then, set up Port Address Translation (PAT) to map the internal IPs and their ports to the outbound public address.
   
   *Example of PAT:* If an internal PC (e.g., `192.168.1.1`) wants to ping an external server (e.g., `201.1.1.2`), PAT replaces the private IP with the router's public IP. The connection mappings will look something like `192.168.1.1:30000` <-> `201.1.1.2:80`. The external server only sees the request coming from `200.1.1.2:800` (router's translated port) and replies to it. The router then receives the reply and forwards it back to `192.168.1.1:30000`.

2. **What is an offsite network?**
   An offsite network is simply a network that is not physically or logically connected within the same local geographical or topological area as the current network.

3. **How to host a remote DHCP server (and service) accessed via a public address?**
   - **On the Server:** First, set up the DHCP services (`Services -> DHCP -> Pools`). Ensure the pool size is sufficient, set the default gateway to the local router's internal IP address, and configure the router interface with a public address. Ensure *Static PAT* is implemented so external packets can reliably reach the server.
   - **On the Router:** The `ip helper-address` utility must be configured on the interface facing the local network. It converts DHCP broadcast packets into unicast packets targeted directly at the public address of the remote DHCP server.

### Configuration Steps & Command Explanations

Here is a line-by-line breakdown of the conceptual commands used for Scenario 1:

```cisco
ip address {target_ip} {subnet_mask}
```
- **Explanation:** Assigns a specific IP address and subnet mask to the current router interface.

```cisco
access-list {number} {permit/deny} {network_id} {wildcard_mask}
```
- **Explanation:** Creates a standard access control list (ACL). This is used here to match a specific internal network (e.g., `192.168.1.0` with wildcard `0.0.0.255`) that is permitted to be translated.

```cisco
ip nat inside source list {access-list-number} interface {name of interface} overload
```
- **Explanation:** Configures Dynamic PAT (Port Address Translation). 
  - `list {access-list-number}` tells the router to translate IP addresses that match the ACL.
  - `interface {name of interface}` specifies the external-facing interface whose public IP will be used.
  - `overload` enables port translation so multiple internal devices can share the single public IP.

```cisco
ip nat inside source static {protocol} {internal_private_ip} {port_number} {external_public_ip} {port_number}
```
- **Explanation:** Configures Static PAT (Port Forwarding). This strictly maps an external public IP and port to an internal private IP and port. It is required to allow outside networks to initiate a connection to an internal server (like the remote DHCP server).

```cisco
ip route 0.0.0.0 0.0.0.0 {next_hop_addr}
```
- **Explanation:** Configures a default static route. `0.0.0.0 0.0.0.0` matches any destination IP. It tells the router to send all unknown external traffic to the specified `{next_hop_addr}`.

```cisco
ip helper-address {target-ip}
```
- **Explanation:** Placed on the internal-facing interface, this command intercepts UDP broadcast packets (like DHCP requests) and forwards them as unicast packets to the specified `{target-ip}` (the DHCP server's IP).

---

## Scenario 2: Static NAT-PT (Network Address Translation - Protocol Translation)

### Context
In this network topology built using GNS3, two subnets exist: one runs exclusively on IPv4 and the other exclusively on IPv6. They are connected via a single Cisco c700 router. The goal is to configure **Static NAT-PT** on the router so devices in the IPv4 subnet can communicate with devices in the IPv6 subnet seamlessly. 

*Note: NAT-PT works by altering the IP packet headers (translating IPv4 to IPv6 and vice versa) as they pass through the router. The end devices are completely unaware of the translation.*

### Requirements
- **Tools:** GNS3 0.8.3.1 (with Wireshark included)
- **Images:** Linux microcore 3.8.2, Router c700 (placed in the `required-images/` folder)

### Configuration Steps & Command Explanations

#### 1. Configuring IPv4 PCs (PC1 & PC2)
These commands are used on Linux Microcore to set up the IPv4 end devices.

```bash
sudo ifconfig eth0 192.168.1.1 netmask 255.255.255.0 up
```
- **Explanation:** Runs with root privileges (`sudo`) to configure the `eth0` network interface with the IP address `192.168.1.1` and subnet mask `255.255.255.0`, and turns the interface `up` (active).

```bash
sudo route add default gw 192.168.1.254
```
- **Explanation:** Adds a default gateway (`192.168.1.254`) to the routing table, directing all non-local traffic to the router.

```bash
sudo ip route add 192.168.2.0/24 via 192.168.1.254
```
- **Explanation:** Explicitly adds a static route for the `192.168.2.0/24` network to go through the gateway `192.168.1.254`. *(This is useful for the simulated mapped addresses we'll use in NAT-PT)*.

```bash
vi /opt/bootlocal.sh
```
- **Explanation:** Opens the `bootlocal.sh` startup script in the `vi` text editor. The IP and route commands should be pasted here so they persist across reboots.

```bash
filetool.sh -b
```
- **Explanation:** A Microcore Linux specific command that backs up changes made to the configuration scripts into the virtual disk, ensuring the `bootlocal.sh` changes are saved permanently.

#### 2. Configuring IPv6 PCs (PC3 & PC4)
These commands configure the IPv6 end devices.

```bash
sudo ip -6 addr add 2000::1/64 dev eth0
```
- **Explanation:** Uses the `ip` tool with the `-6` flag to assign the IPv6 address `2000::1` (with a prefix length of `/64`) to the `eth0` network device.

```bash
sudo ip -6 route add default via 2000::1000
```
- **Explanation:** Sets the default IPv6 route, sending all outbound IPv6 traffic to the gateway address `2000::1000` (the router).

```bash
sudo ip -6 route add 2001::/96 via 2000::1000
```
- **Explanation:** Explicitly adds a static IPv6 route for the `2001::/96` prefix to go through the gateway. This prefix will be used by the router for NAT-PT mapping.

```bash
vi /opt/bootlocal.sh
filetool.sh -b
```
- **Explanation:** Same as the IPv4 setup, these commands modify the `bootlocal.sh` startup script to include the IPv6 network configurations on boot, and then sync those changes to the disk so they are kept permanently.

#### 3. Configuring Static NAT-PT on the Router (R1)
These are the Cisco IOS commands applied to the router to enable IPv4 <-> IPv6 translation.

```cisco
R1> enable
```
- **Explanation:** Transitions the router from User EXEC mode to Privileged EXEC mode (often requires a password).

```cisco
R1# configure terminal
```
- **Explanation:** Enters Global Configuration mode to make changes that affect the router as a whole.

```cisco
R1(config)# ipv6 unicast-routing
```
- **Explanation:** Globally enables the forwarding of IPv6 unicast datagrams. Without this, the router won't route IPv6 traffic.

```cisco
R1(config)# interface FastEthernet0/0
```
- **Explanation:** Enters Interface Configuration mode for the specific port `FastEthernet0/0`.

```cisco
R1(config-if)# ip address 192.168.1.254 255.255.255.0
```
- **Explanation:** Assigns the IPv4 gateway address to this interface for the IPv4 subnet.

```cisco
R1(config-if)# ipv6 nat
```
- **Explanation:** Designates this interface as participating in IPv6 NAT-PT (protocol translation mechanism).

```cisco
R1(config-if)# no shutdown
```
- **Explanation:** Turns on (enables) the interface.

```cisco
R1(config-if)# exit
```
- **Explanation:** Exits Interface Configuration mode and returns to Global Configuration mode.

```cisco
R1(config)# interface FastEthernet0/1
R1(config-if)# ipv6 address 2000::1000/64
R1(config-if)# ipv6 nat
R1(config-if)# no shutdown
R1(config-if)# exit
```
- **Explanation:** Exact same process as above, but configures the second interface (`FastEthernet0/1`) with the IPv6 gateway address `2000::1000/64` and enables `ipv6 nat` translation on it.

```cisco
R1(config)# ipv6 nat prefix 2001::/96
```
- **Explanation:** Defines the IPv6 prefix (`2001::/96`) that will be used by the NAT-PT process to represent translated IPv4 addresses inside the IPv6 network side.

```cisco
# Map IPv4 PCs (PC1, PC2) to the required IPv6 addresses
R1(config)# ipv6 nat v4v6 source 192.168.1.1 2001::1
R1(config)# ipv6 nat v4v6 source 192.168.1.2 2001::2
```
- **Explanation:** These are Static NAT-PT mapping commands. They explicitly tell the router that when IPv4 host `192.168.1.1` communicates with the IPv6 network, its source address shouldn't be IPv4, but translated to the IPv6 address `2001::1` (and similarly for `.2` to `::2`).

```cisco
# Map IPv6 PCs (PC3, PC4) to the required IPv4 addresses
R1(config)# ipv6 nat v6v4 source 2000::1 192.168.2.1
R1(config)# ipv6 nat v6v4 source 2000::2 192.168.2.2
```
- **Explanation:** More Static NAT-PT mapping commands. Here, they map the real IPv6 host (`2000::1`) to a virtual IPv4 address (`192.168.2.1`). Therefore, when the IPv4 PCs want to send packets to the IPv6 PCs, they will route them to this virtual IPv4 address (`192.168.2.1`).

```cisco
write memory
```
- **Explanation:** Saves the running configuration to NVRAM (startup configuration), ensuring the configuration is kept after a router reboot.

```cisco
show startup-config
```
- **Explanation:** Displays the saved startup configuration to the console for verification.
