package com.visionplaymedia.covid19tracker.services;
import com.visionplaymedia.covid19tracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoronaVirusDataService {

    private static String VIRUS_DATA_URL ="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_19-covid-Confirmed.csv";
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct
   @Scheduled(cron ="* * 1 * * *") //run everyday
    public void fetchVirusData() throws IOException, InterruptedException {

       List<LocationStats> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(VIRUS_DATA_URL))
                .build();
        HttpResponse<String> httpResponse =  client.send(request, HttpResponse.BodyHandlers.ofString());
         System.out.println(httpResponse.body());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
//        // Reader in = new FileReader("path/to/file.csv");
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {

          //  String state = record.get("Province/State");
            LocationStats locationStat = new LocationStats();
            locationStat.setState(record.get("Province/State"));
            locationStat.setCountry(record.get("Country/Region"));
            locationStat.setLatestTotalCases(Integer.parseInt(record.get(record.size() - 2)));
            int latestCases = Integer.parseInt(record.get(record.size() -2 ));
            int previousDayCases = Integer.parseInt(record.get(record.size() -3 ));
            locationStat.setLatestTotalCases(latestCases);
            locationStat.setDiffDay(latestCases - previousDayCases);
             System.out.println(locationStat);
             newStats.add(locationStat);
            // System.out.println(state);
        }
       this.allStats = newStats;
    }

}
//