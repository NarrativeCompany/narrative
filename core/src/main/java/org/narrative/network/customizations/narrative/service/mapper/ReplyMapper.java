package org.narrative.network.customizations.narrative.service.mapper;

import org.narrative.network.core.composition.base.Reply;
import org.narrative.network.customizations.narrative.service.api.model.CommentDTO;
import org.narrative.network.customizations.narrative.service.mapper.util.ServiceMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = ServiceMapperConfig.class, uses = {UserMapper.class, RatingMapper.class})
public interface ReplyMapper {
    @Mapping(source = "bodyResolved", target = CommentDTO.Fields.body)
    CommentDTO mapReplyToCommentDTO(Reply reply);

    List<CommentDTO> mapReplyListToCommentDTOList(List<Reply> replies);
}
