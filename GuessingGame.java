// HTTP KIRJASTOJA JAVALLA, JOILLA VOI LUODA HTTP PALVELIMEN
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Random;

public class GuessingGame {

    private static int ArvattavaLuku;       // random luku. jonka peli arpoo
    private static int yritykset;           // arvauskertojen määrä
    private static final int maxYritykset = 10; // Maksimi vastaus määrä

    public static void main(String[] args) throws IOException {
        // ARpoo random numeron
        Random random = new Random();
        ArvattavaLuku = random.nextInt(100) + 1; // Arvotaan luku väliltä 1-100
        yritykset = 0; // asettaa arvaukset aluksi nollaan

        //Luo HTTP-palvelin pyyntöjen käsittelyä varten
        InetSocketAddress address = new InetSocketAddress("localhost", 8081);
        HttpServer server = HttpServer.create(address, 0);

        // Luo /game osoitteen
        server.createContext("/game", new GameHandler());

        // Käynnistä palvelimen
            server.start();

        System.out.println("Server is running on port 8081");
    }

    static class GameHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod(); // Tarkistaa HTTP-pyynnön tyypin GET/post

            if (method.equals("GET")) {
                handleGetRequest(exchange); //Kutsu GET käsittelijää
            } 
            else if (method.equals("POST")) {
                handlePostRequest(exchange); //Kutsu POST käsittelijää
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            // Käsittele GET-pyynnön 
            String response = "Arvaa luku 1 - 100 valilta!.";
            sendResponse(exchange, response);
        }

            private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Käsittele POST-pyynnön
            if (yritykset >= maxYritykset) {
                String response = "Peli on ohi, oikea vastaus olisi ollut " + ArvattavaLuku + ".";
                sendResponse(exchange, response);
            } else {
                String requestBody = new String(exchange.getRequestBody().readAllBytes()).trim();
                try {
                        int guess = Integer.parseInt(requestBody);
                        yritykset++;
                        String response = checkGuess(guess);
                        sendResponse(exchange, response);
                } catch (NumberFormatException e) {
                    String errorResponse = "käytä numeroita.";
                    sendResponse(exchange, errorResponse);
                }
            }
        }

        private String checkGuess(int guess) {
            // Tarkistaa pelaajan arvauksen ja palauttaa
            if (guess == ArvattavaLuku) {
                return "Oikein!";
            } 
                
                else if (yritykset >= maxYritykset) {
                return "Peli on ohi, oikea vastaus on  " + ArvattavaLuku + ".";
            } else if (guess < ArvattavaLuku) {
                return "Luku on liian pieni.";
            } else {
                return "Luku on liian suuri";
            }
        }

        private void sendResponse(HttpExchange exchange, String response) throws IOException {
            // Lähettää vastaus pelaajalle HTTP
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();

            os.write(response.getBytes());
            os.close();
        }
    }
}
