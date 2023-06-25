package kr.goldenmine.inuminecraftlauncher.file;

import kr.goldenmine.inuminecraftlauncher.models.MD5Request;
import kr.goldenmine.inuminecraftlauncher.util.FileUtilKt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    private static String getOSDirectory(String os, String file) {
        return "java/" + os + "/" + file;
    }

    private static String getModDirectory(String mod) {
        return "mods/" + mod;
    }

    private static String getVersionsFile() {
        return "versions.txt";
    }

    private static List<String> versions = new ArrayList<>();

    static {
        // load all available versions
        File file = new File(getVersionsFile());
        try(BufferedReader r = new BufferedReader(new FileReader(file))) {
            String s;
            while((s = r.readLine()) != null) {
                versions.add(s);
                logger.info("version: " + s);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/versions")
    public ResponseEntity<List<String>> getVersions() {
        return ResponseEntity.ok(versions);
    }

//    @PostMapping("/upload")
//    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
//        String fileName = fileStorageService.storeFile(file);
//
//        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
//                .path("/download/")
//                .path(fileName)
//                .toUriString();
//
//        return new UploadFileResponse(fileName, fileDownloadUri,
//                file.getContentType(), file.getSize());
//    }

    @GetMapping("/download/java/{os:.+}/{fileName:.+}")
    public ResponseEntity<Resource> downloadJava(@PathVariable String os, @PathVariable String fileName, HttpServletRequest request) {
        return getDownloadResource("java/" + os + "/" + fileName, request);
    }

    @GetMapping("/download/versions/{version:.+}")
    public ResponseEntity<Resource> downloadVersion(@PathVariable String version, HttpServletRequest request) {
        return getDownloadResource("versions/" + version + ".json", request);
    }

    @GetMapping("/download/mods/{modName:.+}")
    public ResponseEntity<Resource> downloadMod(@PathVariable String modName, HttpServletRequest request) {
        return getDownloadResource("mods/" + modName, request);
    }

    @GetMapping("/download/options/{option:.+}")
    public ResponseEntity<Resource> downloadOption(@PathVariable String option, HttpServletRequest request) {
        return getDownloadResource("options/" + option + ".txt", request);
    }

    @GetMapping("/download/shaders/{shader:.+}")
    public ResponseEntity<Resource> downloadShader(@PathVariable String shader, HttpServletRequest request) {
        return getDownloadResource("shaders/" + shader + ".zip", request);
    }

    public ResponseEntity<Resource> getDownloadResource(String fileName, HttpServletRequest request)  {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/check/java/{os:.+}/{fileName:.+}")
    public ResponseEntity<MD5Request> checkFileJava(@PathVariable String os, @PathVariable String fileName) throws Exception {
        return checkFile("java/" + os + "/" + fileName);
    }

    @GetMapping("/check/versions/{version:.+}")
    public ResponseEntity<MD5Request> checkFileVersion(@PathVariable String version) throws Exception {
        return checkFile("versions/" + version + ".json");
    }

    @GetMapping("/check/mods/{modName:.+}")
    public ResponseEntity<MD5Request> checkFileMods(@PathVariable String modName) throws Exception {
        return checkFile("mods/" + modName);
    }

    @GetMapping("/check/options/{option:.+}")
    public ResponseEntity<MD5Request> checkFileOptions(@PathVariable String option) throws Exception {
        return checkFile("options/" + option + ".txt");
    }

    @GetMapping("/check/shaders/{shader:.+}")
    public ResponseEntity<MD5Request> checkFileShaders(@PathVariable String shader) throws Exception {
        return checkFile("shaders/" + shader + ".zip");
    }

    private HashMap<String, String> md5Cache = new HashMap<>();

    public ResponseEntity<MD5Request> checkFile(String fileName) throws IOException, NoSuchAlgorithmException {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        if(!md5Cache.containsKey(fileName)) {
            md5Cache.put(fileName, FileUtilKt.getFileMD5(resource.getFile().getAbsolutePath()));
        }

        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
        return new ResponseEntity<>(new MD5Request(md5Cache.get(fileName)), headers, HttpStatus.OK);
    }
}