# Nerdle (base)
A library that provides methods for solving the online game [Nerdle](https://nerdlegame.com/). Currently available methods are:
* `Nerdle.getPossibilities` - generates a set of possibilities given any number of guess-feedback pairs
* `Nerdle.sortPossibilities` - sorts the given set of possibilities by the amount of information they are expected to yield

## Usage
### Gradle
First, add the repository, adding the relevant credentials:
```
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/Contrabass26/NerdleBase")
        credentials {
            username = [your GitHub username]
            password = [your access token]
        }
    }
}
```
Then add the dependency:
```
dependencies {
    implementation("com.contrabass:nerdle-base:1.0.0")
}
```

## Building
1. Clone the repository: `git clone https://github.com/Contrabass26/NerdleBase.git`
2. Build the project: `./gradlew build`
3. Source and library JARs can be found in `lib/build/libs` relative to the project root

## Publishing packages
1. Clone the repository: `git clone https://github.com/Contrabass26/NerdleBase.git`
2. Set the environment variables `GPR_USERNAME` and `GPR_TOKEN` to your username and access token respectively
3. Publish the package: `./gradlew publish`
