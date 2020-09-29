package com.example.uaa.web.rest;

import com.example.uaa.config.Constants;
import com.example.uaa.domain.Entry;
import com.example.uaa.service.EntryService;
import com.example.uaa.web.rest.util.PaginationUtil;
import com.example.uaa.web.rest.util.ResponseUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class EntryResource {

    private EntryService entryService;


    /**
     * {@code Get /entries }: get all entries
     *
     * @param pageable the pagination information
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all entries.
     */
    @GetMapping("/entries")
    public ResponseEntity<List<Entry>> getAllEntries(Pageable pageable) {
        log.debug("REST request to get all entries");
        Page<Entry> page = entryService.getAllEntries(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code Get /entries/:id }: get a entry by Id
     *
     * @param id identifier to get a entry
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with a Entry body
     */
    @GetMapping("/entries/{id}")
    public ResponseEntity<Entry> getEntryById(@PathVariable Long id) {
        log.debug("REST request to get a entry by ID {}", id);
        return ResponseUtil.wrapOrNotFound(entryService.getEntryById(id));
    }

    /**
     * {@code Get /entries/:username }: get a entry by username's owner
     *
     * @param username user's identifier of the entry's owner
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with a Entry body
     */
    @GetMapping("/entries/{username:" + Constants.LOGIN_REGEX + "}/username")
    public ResponseEntity<List<Entry>> getEntriesByUsername(@PathVariable String username, Pageable pageable) {
        log.debug("REST request to get a entry by username {}", username);
        Page<Entry> page = entryService.getEntriesByUsername(pageable, username);
        HttpHeaders headers = PaginationUtil.generatePaginationHeaders(
                ServletUriComponentsBuilder.fromCurrentRequest(),
                page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}
