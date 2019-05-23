# Security Policy

## Supported Versions

Supported versions are the versions we support.

Since the project currently does not have any major changes, we always
recommend using latest version, unless explicitly stated in the release notes,
e.g for example "beta / alpha / experimental release / feature"

You have to use (or at least test with) one of the supported versions before reporting
a vulnerability.

Pre Releases are releases we release with a suffix, e.g V13b. We use this to
test the software before releasing a new stable version. Stable releases without a suffix can also
made pre-releases if some vulnerability found in the future.

Since pre releases are not stable versions; they are not supported. In deeper,
they supported; but only if it's the last available version.

If the vulnerability is exists in the latest available pre-release, then
also test with the current stable release.

If it also exists on stable release, that release is also should be marked as pre-release, 
and in either cases, the vulnerability should be fixed before a new stable release.

Since we don't released a new major version yet, we don't have any active
version branches for fixes or security fixes. So, we always provide fixes to latest version.

For the above statement, modifying the old versions are _not_ possible. Of course
you can checkout the specific tag and modify it yourself.

| Version | Supported          |
| ------- | ------------------ |
| All Pre Releases   | :x:                |
| Latest Stable Release   | :white_check_mark: |

We have also some explicit rule for stable releases. The stable releases that contains vulnerabilities or
critical bugs are also marked as pre-releases.

For example, we released 2.2.14 and it included a critical bug first introduced in V13b and left in 2.2.14. 
So, 2.2.14 is _not_ supported, it contains critical bugs / strange issues and it is obsolete now since 2.2.15 is out.

The stable releases (such as 2.2.14, 2.2.15 etc.) that include critical vulnerabilities, bugs, or errors
are marked as pre-releases, and you should be warned in release notes.

If you not understand these statements: Just use the latest available version and you should
be fine.

## Reporting a Vulnerability

You should _not_ report critical security vulnerabilities directly from
issues section.

You should contact one of the maintainers from Discord, with a
private message. Do _not_ post critical security vulnerabilities publicly.

Discord server: https://discord.gg/tmupwqn (Do NOT report it publicly!)  
My Discord address: !💲мυѕтαғα öɴcel#0001

You should clearly describe what the vulnerability, how to reproduce, how to abuse, etc. You should
also link some sites that explains / documents the vulnerability.

Since we also use Snyk for scanning vulnerabilities, you can use the latest version
and you should be fine, without any known vulnerability.
