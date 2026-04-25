package com.study.shop.domain.event;

import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.comment.repository.CommentRepository;
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
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class MemberWithdrawEventListener {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ItemRepository itemRepository;

    private final PostService postService;

    @Async
    @EventListener
    @Transactional
    public void onMemberWithdrawEvent(MemberWithdrawEvent event) {
        postRepository.findByMemberId(event.memberId())
                .forEach(post -> {postService.deletePost(event.memberId(),  post.getId());});

        commentRepository.softDeleteAllByMember(event.memberId());
        itemRepository.softDeleteByMemberId(event.memberId());

        memberRepository.deleteById(event.memberId());
    }
}
