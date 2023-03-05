package kr.goldenmine.inuminecraftlauncher.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.ArrayList;
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

    @PostMapping("/upload")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/download/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize());
    }

    @GetMapping("/download/java/{os:.+}/{fileName:.+}")
    public ResponseEntity<Resource> downloadJava(@PathVariable String os, @PathVariable String fileName, HttpServletRequest request) {
        return getDownloadResource("java/" + os + "/" + fileName, request);
    }

    @GetMapping("/download/mods/{modName:.+}")
    public ResponseEntity<Resource> downloadMod(@PathVariable String modName, HttpServletRequest request) {
        return getDownloadResource("mods/" + modName, request);
    }

    @GetMapping("/download/version/{version:.+}")
    public ResponseEntity<Resource> downloadVersion(@PathVariable("version") String version, HttpServletRequest request) {
        return getDownloadResource("versions/" + version  + ".json", request);
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


//    @GetMapping("/check/{fileName:.+}")
//    public ResponseEntity<String> checkFile(@PathVariable String fileName) throws Exception {
//        Resource resource = fileStorageService.loadFileAsResource(fileName);
//
//        File file = resource.getFile();
//        FileInputStream fileInputStream = new FileInputStream(file);
//        MessageDigest md = MessageDigest.getInstance("MD5");
//
//        byte[] bytes = fileInputStream.readAllBytes();
//        fileInputStream.close();
//
//        md.update(bytes);
//        String hash = Base64.getEncoder().encodeToString(md.digest());
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.TEXT_PLAIN);
//        return new ResponseEntity<>(hash, headers, HttpStatus.OK);
//    }
}