package com.project.yamipick.ai.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tblMenu")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
        name = "SEQ_MENU",
        sequenceName = "SEQMENU",
        allocationSize = 1
)
public class Menu {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MENU")
    @Column(name = "seqMenu")
    private Long seqMenu;

    @Column(name = "menuName", nullable = false, length = 100)
    private String menuName;

    @Column(name = "menuCategory", nullable = false, length = 50)
    private String menuCategory;

    @Column(name = "flavorTags", length = 200)
    private String flavorTags;   // "매운맛,짭짤함" 같은 문자열

    @Column(name = "menuDescription", length = 2000)
    private String menuDescription;
    
    @Column(name = "menuEnglish")
    private String menuEnglish;

}
