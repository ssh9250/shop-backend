package com.study.shop.domain.event;

import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.Item.service.ItemService;
import com.study.shop.domain.comment.repository.CommentRepository;
import com.study.shop.domain.comment.service.CommentService;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class MemberWithdrawEventListener {

    private final PostService postService;
    private final CommentService commentService;
    private final ItemService itemService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 별도 스레드를 위한 새 트랜잭션 (commentService, postService 등)
    public void onMemberWithdrawEvent(MemberWithdrawEvent event) {
        log.info("starting withdraw member id : {}", event.memberId());
        //1. 내가쓴댓글 2. 내가쓴게시글들의 댓글들 3. 내가쓴포스트 첨부파일들 4. 포스트들 5. 아이템들
        commentService.deleteAllCommentByMemberId(event.memberId());
        commentService.deleteAllCommentByMemberPosts(event.memberId());
        postService.deleteAllPostByMemberId(event.memberId());
        itemService.deleteAllItemsByMemberId(event.memberId());

        log.info("member withdraw successfully for member id : {}", event.memberId());
    }
}
