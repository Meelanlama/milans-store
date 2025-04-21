package com.milan.handler;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GenericResponse {

    private HttpStatus responseStatus;

    private String status; //success or failed

    private String message; //msg to show

    private Object data;

    public ResponseEntity<?> create(){
        Map<String,Object> map = new LinkedHashMap<>();

        map.put("status", status);
        map.put("message", message);

        //it's not mandatory, we can send msg only
        if (!ObjectUtils.isEmpty(data)) {
            map.put("data", data);
        }

        return new ResponseEntity<>(map, responseStatus);
    }
}
