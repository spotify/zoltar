from setuptools import setup

setup(
    install_requires=[
        "numpy==1.14.0",
        "tensorflow==1.15.2",
        "xgboost==0.6a2",
        "spotify-tensorflow==0.2.11"
    ],
    dependency_links=[
        "https://pypi.spotify.net/spotify/production"
    ],
    entry_points={
        'console_scripts': [
            'iris-tensorflow = iris.tensorflow:main',
            'iris-xgboost = iris.xgboost:main'
        ]
    }
)
