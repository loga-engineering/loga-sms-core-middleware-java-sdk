# Contributing to Loga SMS Java SDK

Thank you for considering contributing to the Loga SMS Java SDK! We welcome contributions from the community.

## How to Report Issues

Please use the [GitHub Issues](https://github.com/loga-engineering/loga-sms-core-middleware-java-sdk/issues) tab to report bugs or suggest enhancements. Include:

- A clear, descriptive title
- Steps to reproduce the issue
- Expected vs actual behaviour
- SDK version, Java version, and OS

## How to Submit Changes

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes following our commit message convention
4. Push to your fork (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Code Style

This project follows [Google Java Style](https://google.github.io/styleguide/javaguide.html). Please ensure your code is formatted accordingly.

## Testing

Run the test suite before submitting your PR:

```bash
mvn test
```

## Building

Build the project locally:

```bash
mvn clean package
```

## Commit Messages

We use [Conventional Commits](https://www.conventionalcommits.org/). Examples:

- `feat: add support for bulk SMS sending`
- `fix: handle null response in status check`
- `docs: update README configuration table`
- `chore: bump Jackson dependency version`

## Developer Certificate of Origin

By contributing, you agree to the [Developer Certificate of Origin (DCO)](https://developercertificate.org/). All commits must be signed off:

```bash
git commit -s -m "feat: my contribution"
```

This ensures every commit includes the `Signed-off-by:` trailer.
