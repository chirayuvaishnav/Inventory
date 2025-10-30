//package com.inventory;
//
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
///**
// * Controller responsible for handling web requests (HTML views).
// * It maps the root URL ("/") to the index.html file.
// */
//@Controller // Use @Controller for serving views/HTML files
//public class WebController {
//
//    /**
//     * Maps requests to the root URL ("/") and returns the name of the HTML file
//     * to be served (Spring automatically looks for index.html in resources).
//     * @return The name of the view (index.html).
//     */
//    @GetMapping("/")
//    public String redirectToIndex() {
//        return "index";
//    }
//}

//package com.inventory;
//
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * REST Controller responsible for serving the main static HTML file.
// * This approach explicitly returns the index.html file as a resource,
// * bypassing potential view resolver configuration issues.
// */
//@RestController // Use RestController to handle requests directly
//public class WebController {
//
//    /**
//     * Maps the root URL ("/") and serves the index.html file.
//     * Searches for index.html in the classpath (e.g., src/main/resources/static/ or src/main/resources/).
//     * @return The index.html file as a resource.
//     */
//    @GetMapping("/")
//    public ResponseEntity<Resource> serveIndexHtml() {
//        try {
//            // Try looking for the file in the common static folder first.
//            Resource resource = new ClassPathResource("static/index.html");
//
//            // If not found in static/, try the root of resources/.
//            if (!resource.exists()) {
//                resource = new ClassPathResource("static/index.html");
//            }
//
//            // If the file is still not found, return a 404 (or let Spring handle it)
//            if (!resource.exists() || !resource.isReadable()) {
//                // Return a simple 404 response if the file truly can't be read.
//                return ResponseEntity.notFound().build();
//            }
//
//            // Return the file content with the correct MIME type (text/html)
//            return ResponseEntity.ok()
//                    .contentType(MediaType.TEXT_HTML)
//                    .body(resource);
//
//        } catch (Exception e) {
//            // Log the error for debugging purposes in the console
//            e.printStackTrace();
//            // Return an internal server error response
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//}







package com.inventory;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller responsible for serving the main static HTML file.
 * Now serves 'login.html' as the application's entry point.
 */
@RestController // Use RestController to handle requests directly
public class WebController {

    /**
     * Maps the root URL ("/") and serves the login.html file.
     * Searches for login.html in the classpath.
     * @return The login.html file as a resource.
     */
    @GetMapping("/")
    public ResponseEntity<Resource> serveIndexHtml() {
        try {
            // Updated to look for 'login.html'
            Resource resource = new ClassPathResource("login.html");

            // If not found in the root of resources, try the common 'static/' subfolder
            if (!resource.exists()) {
                resource = new ClassPathResource("static/login.html");
            }

            // If the file is still not found, return a 404
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Return the file content with the correct MIME type (text/html)
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
