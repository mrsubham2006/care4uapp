package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.model.AppointmentStatus
import com.example.core.model.BillStatus
import com.example.core.repository.CareNexusRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var repository: CareNexusRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    repository = CareNexusRepository.getInstance(context)
  }

  @Test
  fun `verify app name string resource is CareNexus`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("CareNexus", appName)
  }

  @Test
  fun `verify authentication flow and role authorization`() {
    val success = repository.login("pradhansubham024@gmail.com", "CareNexus2026!")
    assertTrue(success)
    assertTrue(repository.isAuthenticated.value)
    assertEquals("PATIENT", repository.currentPatient.value.role)
  }

  @Test
  fun `verify appointment booking and double booking prevention`() {
    val doc = repository.doctors.first()
    val date = "2026-11-15"
    val slot = "10:30 AM"

    // First booking should succeed
    val res1 = repository.bookAppointment(doc.id, date, slot, "IN_PERSON", "Cardiac Followup")
    assertTrue(res1.isSuccess)

    // Second booking on the same doctor, date and slot should fail (double-booking prevention)
    val res2 = repository.bookAppointment(doc.id, date, slot, "IN_PERSON", "Duplicate Slot")
    assertTrue(res2.isFailure)
  }

  @Test
  fun `verify digital queue advancement`() {
    val initialAhead = repository.queueStatus.value.patientsAhead
    repository.advanceDigitalQueue()
    val newAhead = repository.queueStatus.value.patientsAhead
    assertTrue(newAhead <= initialAhead)
  }

  @Test
  fun `verify bill payment transaction generation`() {
    val bills = repository.bills.value
    val pendingBill = bills.firstOrNull { it.status == BillStatus.PENDING }
    if (pendingBill != null) {
      val payRes = repository.payBill(pendingBill.id, "Visa ending in 4242")
      assertTrue(payRes.isSuccess)
      val updatedBill = repository.bills.value.find { it.id == pendingBill.id }
      assertEquals(BillStatus.PAID, updatedBill?.status)
      assertNotNull(updatedBill?.transactionId)
    }
  }
}
