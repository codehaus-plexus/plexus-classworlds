/**
 * Legacy Classworlds 1.x API, preserved for binary compatibility with Eclipse Sisu.
 *
 * <p>This package has been deprecated since version 2.5.2 (2014). New code should use
 * {@code org.codehaus.plexus.classworlds} instead.</p>
 *
 * <p><strong>Do not remove this package.</strong> The compiled bytecode of
 * {@code org.eclipse.sisu:org.eclipse.sisu.plexus} references {@link ClassRealm},
 * {@link ClassRealmAdapter}, and {@link ClassRealmReverseAdapter} from this package.
 * Removing them causes {@code ClassNotFoundException} at runtime for any application
 * using Sisu, including all Maven 3+ builds.</p>
 *
 * <p>PR #141 removed this package and had to be reverted immediately (commit 223416e).
 * See {@code COMPATIBILITY.md} for details on what Sisu references and what preconditions
 * must be satisfied before any future removal.</p>
 *
 * @deprecated Use {@code org.codehaus.plexus.classworlds} for new code.
 */
package org.codehaus.classworlds;
