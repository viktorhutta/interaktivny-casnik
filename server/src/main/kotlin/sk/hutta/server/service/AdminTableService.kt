package sk.hutta.server.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import sk.hutta.server.dto.AdminDiningTableDto
import sk.hutta.server.dto.DiningTableRequest
import sk.hutta.server.model.DiningTable
import sk.hutta.server.repository.DiningTableRepository
import java.security.SecureRandom
import java.util.Base64

@Service
@Transactional
class AdminTableService(private val diningTableRepository: DiningTableRepository) {

    @Transactional(readOnly = true)
    fun getTables() = diningTableRepository.findAllByOrderById().map { it.toAdminDto() }

    fun createTable(request: DiningTableRequest): AdminDiningTableDto {
        val label = request.label.trim()
        if (label.isEmpty() || label.length > 20) throw badRequest("Označenie musí mať 1 až 20 znakov")
        if (diningTableRepository.existsByLabel(label)) throw conflict("Stôl s týmto označením už existuje")
        return diningTableRepository.save(DiningTable(label, newToken())).toAdminDto()
    }

    // Starý token prestane platiť, preto treba na stôl zapísať nový štítok.
    fun regenerateToken(id: Long): AdminDiningTableDto {
        val table = diningTableRepository.findById(id).orElseThrow { notFound() }
        table.token = newToken()
        return table.toAdminDto()
    }
}

private val random = SecureRandom()

// 32 náhodných bajtov zapísaných ako 43 znakov, ktoré sa dajú bez úprav uložiť do štítku aj poslať v JSON.
private fun newToken(): String {
    val bytes = ByteArray(32)
    random.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

private fun DiningTable.toAdminDto() = AdminDiningTableDto(id!!, label, token)