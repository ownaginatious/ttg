import os
import sys

# Build paths inside the project like this: os.path.join(BASE_DIR, ...)
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.append(os.path.join(BASE_DIR, 'apps/'))

# SECURITY WARNING: keep the secret key used in production secret!
SECRET_KEY = 'd0j!b*lis7mo_n_s@pxubobu$x+i=1aai2mjiamo#^4e5z)^6o'

# SECURITY WARNING: don't run with debug turned on in production!
DEBUG = True

ALLOWED_HOSTS = []

SYSTEM_APPS = []

THIRD_PARTY_APPS = [
    'rest_framework'
]

TTG_APPS = [
    'ttg_model'
]

INSTALLED_APPS = SYSTEM_APPS + THIRD_PARTY_APPS + TTG_APPS

MIDDLEWARE_CLASSES = [
    'django.middleware.locale.LocaleMiddleware'
]

REST_FRAMEWORK = {
    'DEFAULT_PERMISSION_CLASSES': ()
}

ROOT_URLCONF = 'urls'

WSGI_APPLICATION = 'wsgi.application'

# Database
# https://docs.djangoproject.com/en/1.9/ref/settings/#databases

DATABASES = {
    'default': {
        'ENGINE': 'django.db.backends.sqlite3',
        'NAME': os.path.join(BASE_DIR, 'db.sqlite3'),
    }
}

# Internationalization
# https://docs.djangoproject.com/en/1.9/topics/i18n/

LANGUAGE_CODE = 'en-us'

TIME_ZONE = 'UTC'
USE_I18N = True

USE_TZ = True
