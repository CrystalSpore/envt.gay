package gay.envt.controller;

import com.arakelian.jq.*;
import gay.envt.model.DNSRecordJsonBody;
import gay.envt.model.RecordOnlyName;
import gay.envt.model.RecordWithDetails;
import org.springframework.http.*;
import org.springframework.javapoet.ClassName;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

@RestController
public class MainController
{

    // Manages DNS entries on DigitalOcean. Using this API spec for pulling & putting data
    // https://docs.digitalocean.com/reference/api/api-reference/#tag/Domain-Records

    @GetMapping("/getDomainRecords")
    public String getDomainRecords(@RequestParam("domain") String domain)
    {
        Properties secrets = new Properties();
        try {
            secrets.load(ClassName.class.getClassLoader().getResourceAsStream("secret.properties"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Secrets file couldn't load, preventing Digital Ocean API access");
        }
        final String doURI = "https://api.digitalocean.com/v2/domains/" + domain + "/records";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(secrets.getProperty("DIGITALOCEAN_TOKEN"));

        ResponseEntity<String> response = restTemplate.exchange(
                doURI,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class,
                0
        );

        return response.getBody();
    }

    /**
     * Returns status of record existing with the Domain (on Digital Ocean DNS)
     * @param record - Record to check
     * @return true if record exists, false if it does not
     * @throws ResponseStatusException
     *          - Not expected, essentially some part of the
     *          Digital Ocean data model would need to change unexpectedly
     */
    private boolean recordExists(String domain, String record) throws ResponseStatusException
    {
        JqLibrary jqLibrary = ImmutableJqLibrary.of();
        final JqRequest jqRequest = ImmutableJqRequest.builder()
                                    .lib(jqLibrary)
                                    .input(getDomainRecords(domain))
                                    .filter(".domain_records[].name")
                                    .build();
        final JqResponse response = jqRequest.execute();
        if ( response.hasErrors() ) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal JQ process couldn't parse query, which is unexpected due to being a pre-prepped statement");
        }
        else
        {
            String[] responseArray = response.getOutput().split("\n");
            ArrayList<String> recordsList =
                    Arrays.stream(responseArray)
                            .map(s -> s.substring(1)) //remove beginning quote
                            .map(s -> s.substring(0,s.length()-1)) //remove ending quote
                            .filter(s -> !s.equals("@"))
                            .collect(Collectors.toCollection(ArrayList::new));


            System.out.println(recordsList);
            return recordsList.contains(record);
        }
    }

    private String addGenericRecord(String domain, DNSRecordJsonBody jsonBody) throws ResponseStatusException
    {
        boolean exists = recordExists(domain, jsonBody.getName());

        if ( exists )
        {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Conflicts with an existing Record. Use 'Update Record' to change current value of an existing Record");
        }

        Properties secrets = new Properties();
        try {
            secrets.load(ClassName.class.getClassLoader().getResourceAsStream("secret.properties"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Secrets file couldn't load, preventing Digital Ocean API access");
        }

        final String doURI = "https://api.digitalocean.com/v2/domains/" + domain + "/records";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(secrets.getProperty("DIGITALOCEAN_TOKEN"));

        ResponseEntity<String> response = restTemplate.exchange(
                doURI,
                HttpMethod.POST,
                new HttpEntity<>(jsonBody.toMap(), headers),
                String.class,
                0
        );

        return response.getBody();
    }

    @PostMapping("/addARecord")
    public String addARecord(@RequestParam("domain") String domain, @RequestBody RecordWithDetails record) throws ResponseStatusException
    {
        DNSRecordJsonBody jsonBody = new DNSRecordJsonBody("A", record.getName(), record.getData());
        return addGenericRecord(domain, jsonBody);
    }

    @PostMapping("/addTXTRecord")
    public String addTXTRecord(@RequestParam("domain") String domain, @RequestBody RecordWithDetails record) throws ResponseStatusException
    {
        DNSRecordJsonBody jsonBody = new DNSRecordJsonBody("TXT", "_atproto." + record.getName(), record.getData());
        return addGenericRecord(domain, jsonBody);
    }

    private int getRecordIDFromName(String domain, String recordName)
    {
        System.out.println("Start of getRecordIDFromName");
        JqLibrary jqLibrary = ImmutableJqLibrary.of();
        final JqRequest jqRequest = ImmutableJqRequest.builder()
                .lib(jqLibrary)
                .input(getDomainRecords(domain))
                .filter(".domain_records[] | select(.name==\"" + recordName + "\") | .id")
                .build();
        final JqResponse response = jqRequest.execute();
        if ( response.hasErrors() ) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Internal JQ process couldn't parse query, which is unexpected due to being a pre-prepped statement");
        }
        else {
            String[] responseArray = response.getOutput().split("\n");
            ArrayList<String> recordsList = Arrays.stream(responseArray).collect(Collectors.toCollection(ArrayList::new));

            if (recordsList.size() != 1)
            {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Either resource is not available, or there is duplicate data. In either case, contact the administrator for additional assistance");
            }

            int recordID = Integer.parseInt(recordsList.get(0));

            System.out.println(recordID);
            return recordID;
        }

    }

    private String updateGenericRecord(String domain, DNSRecordJsonBody jsonBody) throws ResponseStatusException
    {
        boolean exists = recordExists(domain, jsonBody.getName());

        if ( !exists )
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Record doos not exist. Use 'Add Record' to create a new Record");
        }

        Properties secrets = new Properties();
        try {
            secrets.load(ClassName.class.getClassLoader().getResourceAsStream("secret.properties"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Secrets file couldn't load, preventing Digital Ocean API access");
        }

        final String doURI = "https://api.digitalocean.com/v2/domains/" + domain + "/records/" + getRecordIDFromName(domain, jsonBody.getName());

        System.out.println(doURI);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(secrets.getProperty("DIGITALOCEAN_TOKEN"));

        ResponseEntity<String> response = restTemplate.exchange(
                doURI,
                HttpMethod.PUT,
                new HttpEntity<>(jsonBody.toMap(), headers),
                String.class,
                0
        );

        return response.getBody();
    }

    @PutMapping("/updateARecord")
    public String updateARecord(@RequestParam("domain") String domain, @RequestBody RecordWithDetails record) throws ResponseStatusException
    {
        System.out.println("test");
        DNSRecordJsonBody jsonBody = new DNSRecordJsonBody("A", record.getName(), record.getData());
        return updateGenericRecord(domain, jsonBody);
    }

    @PutMapping("/updateTXTRecord")
    public String updateTXTRecord(@RequestParam("domain") String domain, @RequestBody RecordWithDetails record) throws ResponseStatusException
    {
        DNSRecordJsonBody jsonBody = new DNSRecordJsonBody("TXT", "_atproto." + record.getName(), record.getData());
        return updateGenericRecord(domain, jsonBody);
    }

    private String deleteGenericRecord(String domain, DNSRecordJsonBody jsonBody) throws ResponseStatusException
    {
        boolean exists = recordExists(domain, jsonBody.getName());

        if ( !exists )
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Record name for this type does not exist. Validate entry of record name is correct");
        }

        Properties secrets = new Properties();
        try {
            secrets.load(ClassName.class.getClassLoader().getResourceAsStream("secret.properties"));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Secrets file couldn't load, preventing Digital Ocean API access");
        }
        final String doURI = "https://api.digitalocean.com/v2/domains/" + domain + "/records/" + getRecordIDFromName(domain, jsonBody.getName());

        System.out.println(doURI);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(secrets.getProperty("DIGITALOCEAN_TOKEN"));

        ResponseEntity<String> response = restTemplate.exchange(
                doURI,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class,
                0
        );

        return response.getBody();
    }

    @DeleteMapping("/deleteARecord")
    public String deleteARecord(@RequestParam("domain") String domain, @RequestBody RecordOnlyName record) throws ResponseStatusException
    {
        DNSRecordJsonBody jsonBody = new DNSRecordJsonBody("A", record.getName(), "N/A");
        return deleteGenericRecord(domain, jsonBody);
    }

    @DeleteMapping("/deleteTXTRecord")
    public String deleteTXTRecord(@RequestParam("domain") String domain, @RequestBody RecordOnlyName record) throws ResponseStatusException
    {
        DNSRecordJsonBody jsonBody = new DNSRecordJsonBody("TXT", "_atproto." + record.getName(), "N/A");
        return deleteGenericRecord(domain, jsonBody);
    }

}
