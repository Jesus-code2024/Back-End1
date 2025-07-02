from django.contrib import admin
from .models import Evento, Carrera, Departamento, TipoEvento

@admin.register(Evento)
class EventoAdmin(admin.ModelAdmin):
    list_display = ('titulo', 'tipo_evento', 'estado', 'fecha_inicio', 'carrera')
    list_filter = ('estado', 'tipo_evento', 'carrera')
    search_fields = ('titulo', 'descripcion')
    ordering = ('-fecha_inicio',)

admin.site.site_header = "Panel de Administración de Eventos"
admin.site.site_title = "Gestión de Eventos Educativos"
admin.site.index_title = "Bienvenido al Panel del Administrador"

admin.site.register(Carrera)
admin.site.register(Departamento)
admin.site.register(TipoEvento)
