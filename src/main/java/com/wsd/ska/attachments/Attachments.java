package com.wsd.ska.attachments;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.wsd.ska.messages.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
@Entity
@Table(name = "msg_attachments")
public class Attachments {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String name;


    @Lob
    @Column(nullable = false)
    private String paths;

    @Column(nullable = true)
    private boolean image;

    @Column(nullable = true)
    private boolean audio;

    @Column(nullable = true)
    private boolean video;

    @Column(nullable = true)
    private boolean pdf;

    @Column(nullable = true)
    private boolean excel;

    @Column(nullable = true)
    private boolean word;

    @Column(nullable = true)
    private boolean txt;

    @Column(nullable = true)
    private boolean archive;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "message_id", nullable = true, referencedColumnName = "id")
    @JsonBackReference
    private Message message;

    public Attachments(String filename, String path, boolean image, boolean audio, boolean video, boolean pdf, boolean excel, boolean word, boolean txt, boolean archive){
        this.name = filename;
        this.paths = path;
        this.image = image;
        this.audio = audio;
        this.video = video;
        this.pdf = pdf;
        this.excel = excel;
        this.word = word;
        this.txt = txt;
        this.archive = archive;
    }


}
