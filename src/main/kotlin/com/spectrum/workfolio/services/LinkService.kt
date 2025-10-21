package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Link
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.LinkRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LinkService(
    private val resumeQueryService: ResumeQueryService,
    private val linkRepository: LinkRepository,
) {

    @Transactional(readOnly = true)
    fun getLink(id: String): Link {
        return linkRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_LINK.message) }
    }

    @Transactional(readOnly = true)
    fun listLinks(resumeId: String): List<Link> {
        val resume = resumeQueryService.getResume(resumeId)
        return linkRepository.findByResumeId(resume.id)
    }

    @Transactional
    fun createLink(resumeId: String, url: String, isVisible: Boolean): Link {
        val resume = resumeQueryService.getResume(resumeId)
        val link = Link(
            url = url,
            isVisible = isVisible,
            resume = resume,
        )

        return linkRepository.save(link)
    }

    @Transactional
    fun updateLink(id: String, url: String, isVisible: Boolean): Link {
        val link = this.getLink(id)

        link.changeInfo(
            url = url,
            isVisible = isVisible,
        )

        return linkRepository.save(link)
    }

    @Transactional
    fun deleteLink(id: String) {
        val link = this.getLink(id)
        linkRepository.delete(link)
    }

    @Transactional
    fun deleteLinksByResumeId(resumeId: String) {
        val links = linkRepository.findByResumeId(resumeId)
        linkRepository.deleteAll(links)
    }
}
