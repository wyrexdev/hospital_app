# Hospital Appointment Application

## Installation Guide

### 1. Server Requirements

To run the application, you need a virtual server with Ubuntu installed. For a budget-friendly option, you can rent a server from [Contabo](https://contabo.com/) for around €4/month.

> **Note:** Contabo provides IPv6 support.

---

### 2. Required Packages

#### 2.1. Mail Server Configuration

You need to configure your server for email sending. You can use the `emailwiz` tool for easy setup.

- If your server **has IPv6 support**, you can use `emailwiz` without any changes.
- If your server **does not have IPv6**, comment out the IPv6 configuration lines inside `emailwiz`.

#### 2.2. Node.js

Install **Node.js version 20.x** on your server.

#### 2.3. Nginx

Install **Nginx** as a reverse proxy and for routing.

#### 2.4. Database - ScyllaDB

The application uses **ScyllaDB** (Cassandra-compatible) as the database. After installation, you can access it via the terminal using the `cqlsh` command.

#### 2.5. Webmail (Optional)

Optionally, you can install **Roundcube** with **PHP 8.3** to manage your emails via a web interface.

---

### 3. Domain Configuration

Since Android does not support secure direct IP connections, you must use a domain for secure access.

#### DNS Setup Options:

**Option 1:** Set up your own BIND DNS server and custom NS records.  
**Option 2:** Directly add DNS records (A, MX, AAAA, TXT). *(This is the preferred option.)*

#### Steps:

1. **A Record:** Add an A record pointing to your server's IP address.
2. **MX Record:** Create an MX record for `mail.domain.com` with a priority of 10.
3. **TXT Records:** After running the `emailwiz` tool, it will provide you with 3 TXT records. Add these records to your domain's DNS settings.

---

### 4. OAuth - Google Sign-In Configuration

Create a new OAuth application in the [Google Cloud Console](https://console.cloud.google.com/). Copy the **Client ID** and add it to your `.env` file.

---

### 5. Environment Variables - `.env` File

Fill in the `.env` file with the following information:

- `DATACENTER`
- `SERVER_IP`
- `SCYLLA_KEYSPACE`
- `GOOGLE_CLIENT_ID`

Use the values you obtained during the setup process.

---

### 6. Domain Binding on Android

After linking your backend to your domain, replace any occurrences of `kisetsuna.com` in the Android client with your actual domain name.

---

### ✅ Setup Complete

If you've successfully followed all the steps above, your Hospital Appointment Application is now ready to use.

---

### 🇹🇷 Note About Language

This project is primarily developed in **Turkish**, including variable names, comments, and code structure. We apologize for any inconvenience this may cause and appreciate your understanding.

---

### ❓ Need Help?

If you run into any issues, feel free to reach out to me on Discord: **`wyrex`**