package de.laurel.lorbeerwalls.sdk;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Offsets {
    private static final String OFFSETS_URL = "https://raw.githubusercontent.com/a2x/cs2-dumper/main/output/offsets.json";
    private static final String CLIENT_DLL_URL = "https://raw.githubusercontent.com/a2x/cs2-dumper/main/output/client_dll.json";

    private final Map<String, Integer> offsetMap = new HashMap<>();
    private final Consumer<String> logger;

    public int dwEntityList;
    public int dwViewMatrix;
    public int dwLocalPlayerPawn;
    public int m_iHealth;
    public int m_iTeamNum;
    public int m_hPlayerPawn;
    public int m_pGameSceneNode;
    public int m_modelState;


    public Offsets(Consumer<String> logger) {
        this.logger = logger;
    }

    public boolean load() {
        logger.accept("loading offsets");
        try {
            fetchAndParse(OFFSETS_URL, this::parseAndStoreOffsets, "offsets.json");
            fetchAndParse(CLIENT_DLL_URL, this::parseAndStoreClientDll, "client_dll.json");
            assignOffsets();
            return true;
        } catch (Exception e) {
            logger.accept("fatal error loading offsets: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void fetchAndParse(String url, Consumer<String> parser, String fileName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            logger.accept(fileName + " downloaded");
            parser.accept(response.body());
        } else {
            throw new Exception("error downloading " + fileName + " status: " + response.statusCode());
        }
    }

    private void parseAndStoreOffsets(String jsonContent) {
        JSONObject root = new JSONObject(jsonContent);
        JSONObject clientDllOffsets = root.getJSONObject("client.dll");
        for (String name : clientDllOffsets.keySet()) {
            offsetMap.put(name, clientDllOffsets.getInt(name));
        }
    }

    private void parseAndStoreClientDll(String jsonContent) {
        JSONObject root = new JSONObject(jsonContent);
        JSONObject clientDll = root.getJSONObject("client.dll");
        JSONObject classes = clientDll.getJSONObject("classes");
        for (String className : classes.keySet()) {
            JSONObject classInfo = classes.getJSONObject(className);
            if (classInfo.has("fields")) {
                JSONObject fields = classInfo.getJSONObject("fields");
                for (String fieldName : fields.keySet()) {
                    offsetMap.put(fieldName, fields.getInt(fieldName));
                }
            }
        }
    }

    private void assignOffsets() {
        dwEntityList = get("dwEntityList");
        dwViewMatrix = get("dwViewMatrix");
        dwLocalPlayerPawn = get("dwLocalPlayerPawn");
        m_iHealth = get("m_iHealth");
        m_iTeamNum = get("m_iTeamNum");
        m_hPlayerPawn = get("m_hPlayerPawn");
        m_pGameSceneNode = get("m_pGameSceneNode");
        m_modelState = get("m_modelState");
        logger.accept("all offsets assigned");
    }

    private int get(String name) {
        if (!offsetMap.containsKey(name)) {
            logger.accept("error offset '" + name + "' not found");
            throw new IllegalStateException("Missing critical offset: " + name);
        }
        int value = offsetMap.get(name);
        logger.accept("offset " + name + " = 0x" + Integer.toHexString(value));
        return value;
    }
}
