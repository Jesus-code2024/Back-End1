from django.urls import path, include
from rest_framework.routers import DefaultRouter
from .views import EventoViewSet, DepartamentoViewSet, CarreraViewSet, TipoEventoViewSet, EventosPublicosList, UserViewSet

router = DefaultRouter()
router.register(r'eventos', EventoViewSet)
router.register(r'departamentos', DepartamentoViewSet)
router.register(r'carreras', CarreraViewSet)
router.register(r'tipos', TipoEventoViewSet)
router.register(r'usuarios', UserViewSet)

urlpatterns = [
    path('', include(router.urls)),
    path('eventos-publicos/', EventosPublicosList.as_view(), name='eventos-publicos'),  # << AGREGADO
]
