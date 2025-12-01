package com.project.yamipick.ai.dto;

import com.project.yamipick.ai.entity.Menu;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuDTO {

    private Long seqMenu;
    private String menuName;
    private String menuCategory;
    private String flavorTags;
    private String menuDescription;
    private String menuImage;

    public Menu toEntity() {
        return Menu.builder()
                .seqMenu(this.seqMenu)
                .menuName(this.menuName)
                .menuCategory(this.menuCategory)
                .flavorTags(this.flavorTags)
                .menuDescription(this.menuDescription)
                .menuImage(this.menuImage)
                .build();
    }
}
