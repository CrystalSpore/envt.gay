# ENVT.gay backend handler

## What does this project do?
This codebase is designed to handle the management of providing subdomains
for a root domain (project programmatically any hostname could be used, but the
intent for this project is to be used with the ENVT.gay domain).

## What endpoints exist in the project
Current endpoints include (DO below refers to Digital Ocean):

All Endpoints have a query parameter of: domain -> Domain to check in DO API <br/>
e.g. `http://example.com/getDomainRecords?domain=envt.gay`

| Endpoint            | Request Type | Additional Body Parameters | What it does                                                                                                                                       |
|---------------------|:------------:|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `/getDomainRecords` |    `GET`     | None                       | Provides all records under a domain within DO DNS API. (This includes "@" records provided by default by DO)                                       |
| `/addARecord`       |    `POST`    | `name`, `data`             | Adds an A Record for the given `name` (which is the subdomain), pointing to specific `data` (normally an IPv4 address)                             |
| `/addTXTRecord`     |    `POST`    | `name`, `data`             | Adds an _atproto TXT Record for the given `name` (so `_atproto.name.domain`). Currently built specifically for use with Blue Sky atproto usernames |
| `/updateARecord`    |    `PUT`     | `name`, `data`             | Validates that A Record for the given `name` exists, & updates the `data` if it does. Otherwise returns an error.                                  |
| `/updateTXTRecord`  |    `PUT`     | `name`, `data`             | Validates that an _atproto TXT Record for the given `name` exists, & updates the `data` if it does. Otherwise returns an error.                    |
| `/deleteARecord`    |    `DEL`     | `name`                     | Validates that A Record for the given `name` exists, & deletes the record if it does. Otherwise returns an error.                                  |
| `/deleteTXTRecord`  |    `DEL`     | `name`                     | Validates that an _atproto TXT Record for the given `name` exists, & deletes the if it does. Otherwise returns an error.                           |

## Currently there is no authorization. Do you plan to change that?
YES. Definitely so. I intend to productionalize this repo, as this will be actually used.
I intend to add OAuth2 login via GitHub, & (probably) use JWT tokens for securing the endpoints

## The endpoints always throw an error, stating `Secrets file couldn't load, preventing Digital Ocean API access`
Create a `secrets.properties` file, with value of `DIGITALOCEAN_TOKEN=<Your Token Here>`.
This file is in the `.gitignore`, so you can safely use it during dev work,
or if you are deploying this as an app, then put the file in the same directory as the compiled JAR file