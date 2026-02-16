package com.seretail.inventarios.data.remote

import com.seretail.inventarios.data.remote.dto.ActivoFijoProductoDto
import com.seretail.inventarios.data.remote.dto.CreateSessionRequest
import com.seretail.inventarios.data.remote.dto.ActivoFijoSessionDto
import com.seretail.inventarios.data.remote.dto.ActivoFijoUploadRequest
import com.seretail.inventarios.data.remote.dto.EmpresaDto
import com.seretail.inventarios.data.remote.dto.InventarioSessionDto
import com.seretail.inventarios.data.remote.dto.InventarioUploadRequest
import com.seretail.inventarios.data.remote.dto.LoginRequest
import com.seretail.inventarios.data.remote.dto.LoginResponse
import com.seretail.inventarios.data.remote.dto.LoteDto
import com.seretail.inventarios.data.remote.dto.NoEncontradoUploadRequest
import com.seretail.inventarios.data.remote.dto.PaginatedResponse
import com.seretail.inventarios.data.remote.dto.RfidTagUploadRequest
import com.seretail.inventarios.data.remote.dto.ProductoDto
import com.seretail.inventarios.data.remote.dto.StatusDto
import com.seretail.inventarios.data.remote.dto.SucursalDto
import com.seretail.inventarios.data.remote.dto.TraspasoUploadRequest
import com.seretail.inventarios.data.remote.dto.UploadResponse
import com.seretail.inventarios.data.remote.dto.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Auth
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("logout")
    suspend fun logout(): Response<Unit>

    @GET("me")
    suspend fun me(): Response<UserDto>

    // Sync - Catalogs
    @GET("empresas")
    suspend fun getEmpresas(): Response<List<EmpresaDto>>

    @GET("empresas/{id}/sucursales")
    suspend fun getSucursales(@Path("id") empresaId: Long): Response<List<SucursalDto>>

    @GET("empresas/{id}/productos")
    suspend fun getProductos(
        @Path("id") empresaId: Long,
        @Query("page") page: Int = 1,
    ): Response<PaginatedResponse<ProductoDto>>

    @GET("empresas/{id}/lotes")
    suspend fun getLotes(@Path("id") empresaId: Long): Response<List<LoteDto>>

    @GET("statuses")
    suspend fun getStatuses(): Response<List<StatusDto>>

    // Inventory Sessions
    @GET("inventarios")
    suspend fun getInventarios(): Response<List<InventarioSessionDto>>

    @POST("inventarios/create")
    suspend fun createInventario(@Body request: CreateSessionRequest): Response<InventarioSessionDto>

    @POST("inventarios/upload")
    suspend fun uploadInventario(@Body request: InventarioUploadRequest): Response<UploadResponse>

    // Activo Fijo Sessions
    @GET("activo-fijo")
    suspend fun getActivoFijoSessions(): Response<List<ActivoFijoSessionDto>>

    @POST("activo-fijo/create")
    suspend fun createActivoFijoSession(@Body request: CreateSessionRequest): Response<ActivoFijoSessionDto>

    @GET("activo-fijo-productos")
    suspend fun getActivoFijoProductos(
        @Query("inventario_id") inventarioId: Long? = null,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 500,
    ): Response<PaginatedResponse<ActivoFijoProductoDto>>

    @POST("activo-fijo/upload")
    suspend fun uploadActivoFijo(@Body request: ActivoFijoUploadRequest): Response<UploadResponse>

    @POST("activo-fijo/no-encontrados")
    suspend fun uploadNoEncontrados(@Body request: NoEncontradoUploadRequest): Response<UploadResponse>

    @POST("activo-fijo/traspasos")
    suspend fun uploadTraspasos(@Body request: TraspasoUploadRequest): Response<UploadResponse>

    @POST("activo-fijo/rfid-tags")
    suspend fun uploadRfidTags(@Body request: RfidTagUploadRequest): Response<UploadResponse>

    // Image Upload
    @Multipart
    @POST("activo-fijo/upload-imagen")
    suspend fun uploadImagen(
        @Part("registro_id") registroId: RequestBody,
        @Part("campo") campo: RequestBody,
        @Part imagen: MultipartBody.Part,
    ): Response<UploadResponse>
}
