from django.contrib import admin
from django.urls import path, include
from eventos.views import CustomAuthToken  # ✅ Importa correctamente
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/', include('eventos.urls')),
    path('api/login/', CustomAuthToken.as_view(), name='admin-login'),
]
