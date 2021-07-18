/*
 *  Copyright (C) 2004-2021 Savoir-faire Linux Inc.
 *
 *  Author: Adrien Beraud <adrien.beraud@savoirfairelinux.com>
 *          Alexandre Lision <alexandre.lision@savoirfairelinux.com>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cx.ring.client

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.AdapterView
import android.widget.CompoundButton
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cx.ring.BuildConfig
import cx.ring.R
import cx.ring.about.AboutFragment
import cx.ring.account.AccountEditionFragment
import cx.ring.account.AccountWizardActivity
import cx.ring.application.JamiApplication
import cx.ring.contactrequests.ContactRequestsFragment
import cx.ring.databinding.ActivityHomeBinding
import cx.ring.fragments.ConversationFragment
import cx.ring.fragments.SmartListFragment
import cx.ring.interfaces.BackHandlerInterface
import cx.ring.interfaces.Colorable
import cx.ring.service.DRingService
import cx.ring.services.ContactServiceImpl
import cx.ring.settings.SettingsFragment
import cx.ring.settings.VideoSettingsFragment
import cx.ring.settings.pluginssettings.PluginDetails
import cx.ring.settings.pluginssettings.PluginPathPreferenceFragment
import cx.ring.settings.pluginssettings.PluginSettingsFragment
import cx.ring.settings.pluginssettings.PluginsListSettingsFragment
import cx.ring.utils.BitmapUtils
import cx.ring.utils.ContentUriHandler
import cx.ring.utils.ConversationPath
import cx.ring.utils.DeviceUtils
import cx.ring.views.AvatarFactory
import cx.ring.views.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Predicate
import io.reactivex.rxjava3.schedulers.Schedulers
import net.jami.facades.ConversationFacade
import net.jami.model.Account
import net.jami.model.AccountConfig
import net.jami.model.Conversation
import net.jami.model.Uri
import net.jami.services.AccountService
import net.jami.services.ContactService
import net.jami.services.NotificationService
import net.jami.smartlist.SmartListViewModel
import java.util.concurrent.Future
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener,
    AdapterView.OnItemSelectedListener, Colorable {
    private var fContent: Fragment? = null
    private var fConversation: ConversationFragment? = null
    private var mAccountAdapter: AccountSpinnerAdapter? = null
    private var mAccountFragmentBackHandlerInterface: BackHandlerInterface? = null
    private var mOutlineProvider: ViewOutlineProvider? = null
    private var mOrientation = 0

    @Inject lateinit
    var mContactService: ContactService

    @Inject lateinit
    var mAccountService: AccountService

    @Inject lateinit
    var mConversationFacade: ConversationFacade

    @Inject lateinit
    var mNotificationService: NotificationService

    private var mBinding: ActivityHomeBinding? = null
    private var mMigrationDialog: AlertDialog? = null

    //private String mAccountWithPendingrequests = null;
    private val mDisposable = CompositeDisposable()

    /* called before activity is killed, e.g. rotation */
    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putInt("orientation", mOrientation)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mOrientation = savedInstanceState.getInt("orientation")
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        JamiApplication.instance?.startDaemon()
        mBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(mBinding!!.root)

        // dependency injection
        //JamiApplication.getInstance().getInjectionComponent().inject(this);
        setSupportActionBar(mBinding!!.mainToolbar)
        val ab = supportActionBar
        if (ab != null) {
            ab.title = ""
        }
        mBinding!!.navigationView.setOnNavigationItemSelectedListener(this)
        mBinding!!.navigationView.menu.getItem(NAVIGATION_CONVERSATIONS).isChecked = true
        mOutlineProvider = mBinding!!.appBar.outlineProvider
        if (!DeviceUtils.isTablet(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.bottom_navigation)
        }
        mBinding!!.spinnerToolbar.onItemSelectedListener = this
        mBinding!!.accountSwitch.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            enableAccount(isChecked)
        }
        mBinding!!.contactImage?.setOnClickListener { fConversation?.openContact() }
        handleIntent(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mMigrationDialog != null) {
            if (mMigrationDialog!!.isShowing) mMigrationDialog!!.dismiss()
            mMigrationDialog = null
        }
        fContent = null
        mDisposable.dispose()
        mBinding = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Log.d(TAG, "handleIntent: $intent")
        val extra = intent.extras
        val action = intent.action
        if (ACTION_PRESENT_TRUST_REQUEST_FRAGMENT == action) {
            if (extra?.getString(ContactRequestsFragment.ACCOUNT_ID) == null) {
                return
            }
            //mAccountWithPendingrequests = extra.getString(ContactRequestsFragment.ACCOUNT_ID);
            presentTrustRequestFragment(extra.getString(ContactRequestsFragment.ACCOUNT_ID))
        } else if (Intent.ACTION_SEND == action || Intent.ACTION_SEND_MULTIPLE == action) {
            val path = ConversationPath.fromBundle(extra)
            if (path != null) {
                startConversation(path)
            } else {
                intent.setClass(applicationContext, ShareActivity::class.java)
                startActivity(intent)
            }
        } else if (DRingService.ACTION_CONV_ACCEPT == action || Intent.ACTION_VIEW == action) {
            startConversation(ConversationPath.fromIntent(intent))
        } //else {
        val fragmentManager = supportFragmentManager
        fContent = fragmentManager.findFragmentById(R.id.main_frame)
        if (fContent == null || Intent.ACTION_SEARCH == action) {
            if (fContent is SmartListFragment) {
                (fContent as SmartListFragment).handleIntent(intent)
            } else {
                val content = SmartListFragment()
                fContent = content
                fragmentManager.beginTransaction()
                    .replace(R.id.main_frame, content, HOME_TAG)
                    .commitNow()
            }
        }
        /*if (mAccountWithPendingrequests != null) {
                presentTrustRequestFragment(mAccountWithPendingrequests);
                mAccountWithPendingrequests = null;
            }*/
        //}
    }

    private fun showMigrationDialog() {
        if (mMigrationDialog != null) {
            return
        }
        mMigrationDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.account_migration_title_dialog)
            .setMessage(R.string.account_migration_message_dialog)
            .setIcon(R.drawable.baseline_warning_24)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                selectNavigationItem(R.id.navigation_settings)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setToolbarState(@StringRes titleRes: Int) {
        setToolbarState(getString(titleRes), null)
    }

    private fun setToolbarState(title: String?, subtitle: String?) {
        mBinding!!.mainToolbar.logo = null
        mBinding!!.mainToolbar.title = title
        mBinding!!.mainToolbar.subtitle = subtitle
    }

    private fun showProfileInfo() {
        mBinding!!.spinnerToolbar.visibility = View.VISIBLE
        mBinding!!.mainToolbar.title = null
        mBinding!!.mainToolbar.subtitle = null
    }

    override fun onStart() {
        super.onStart()
        mDisposable.add(
            mAccountService.observableAccountList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { accounts: List<Account> ->
                    if (accounts.isEmpty()) {
                        startActivity(Intent(this, AccountWizardActivity::class.java))
                    }
                    for (account in accounts) {
                        if (account.needsMigration()) {
                            showMigrationDialog()
                            break
                        }
                    }
                })
        mDisposable.add(
            mAccountService.observableAccountList
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ accounts: List<Account?> ->
                    if (mAccountAdapter == null) {
                        mAccountAdapter =
                            AccountSpinnerAdapter(this@HomeActivity, ArrayList(accounts))
                        mAccountAdapter!!.setNotifyOnChange(false)
                        mBinding!!.spinnerToolbar.adapter = mAccountAdapter
                    } else {
                        mAccountAdapter!!.clear()
                        mAccountAdapter!!.addAll(accounts)
                        mAccountAdapter!!.notifyDataSetChanged()
                        if (accounts.isNotEmpty()) {
                            mBinding!!.spinnerToolbar.setSelection(0)
                        }
                    }
                    if (fContent is SmartListFragment) {
                        showProfileInfo()
                    }
                }) { e: Throwable? -> Log.e(TAG, "Error loading account list !", e) })
        mDisposable.add(mAccountService
            .currentAccountSubject
            .switchMap { obj: Account -> obj.unreadPending }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count: Int -> setBadge(R.id.navigation_requests, count) })
        mDisposable.add(mAccountService
            .currentAccountSubject
            .switchMap { obj: Account -> obj.unreadConversations }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { count: Int -> setBadge(R.id.navigation_home, count) })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mDisposable.add(mAccountService
                .currentAccountSubject
                .flatMap { obj: Account -> obj.conversationsSubject }
                .observeOn(Schedulers.io())
                .subscribe({ conversations: List<Conversation> ->
                    setShareShortcuts(
                        conversations
                    )
                }) { e: Throwable? ->
                    Log.e(
                        TAG, "Error generating conversation shortcuts", e
                    )
                })
        }
        if (fConversation == null) fConversation = supportFragmentManager.findFragmentByTag(
            ConversationFragment::class.java.simpleName
        ) as ConversationFragment?
        val newOrientation = resources.configuration.orientation
        if (mOrientation != newOrientation) {
            mOrientation = newOrientation
            hideTabletToolbar()
            if (DeviceUtils.isTablet(this)) {
                selectNavigationItem(R.id.navigation_home)
                showTabletToolbar()
            } else {
                // Remove ConversationFragment that might have been restored after an orientation change
                if (fConversation != null) {
                    supportFragmentManager
                        .beginTransaction()
                        .remove(fConversation!!)
                        .commitNow()
                    fConversation = null
                }
            }
        }

        // Select first conversation in tablet mode
        if (DeviceUtils.isTablet(this)) {
            val intent = intent
            val uri = intent?.data
            if ((intent == null || uri == null) && fConversation == null) {
                var smartlist: Observable<List<Observable<SmartListViewModel>>>? = null
                if (fContent is SmartListFragment) smartlist =
                    mConversationFacade.getSmartList(false) else if (fContent is ContactRequestsFragment) smartlist =
                    mConversationFacade.pendingList
                if (smartlist != null) {
                    mDisposable.add(smartlist
                        .filter(Predicate { list: List<Observable<SmartListViewModel>> -> list.isNotEmpty() })
                        .map { list: List<Observable<SmartListViewModel>> -> list[0].firstOrError() }
                        .firstElement()
                        .flatMapSingle { e: Single<SmartListViewModel>? -> e }
                        .subscribe { element: SmartListViewModel ->
                            startConversation(
                                element.accountId,
                                element.uri
                            )
                        })
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mDisposable.clear()
    }

    fun startConversation(conversationId: String?) {
        mDisposable.add(
            mAccountService.currentAccountSubject
                .firstElement()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { account: Account ->
                    startConversation(
                        account.accountID,
                        Uri.fromString(conversationId)
                    )
                })
    }

    fun startConversation(accountId: String?, conversationId: Uri?) {
        startConversation(ConversationPath(accountId, conversationId))
    }

    private fun startConversation(path: ConversationPath) {
        Log.w(TAG, "startConversation $path")
        if (!DeviceUtils.isTablet(this)) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    path.toUri(),
                    this,
                    ConversationActivity::class.java
                )
            )
        } else {
            startConversationTablet(path.toBundle())
        }
    }

    private fun startConversationTablet(bundle: Bundle?) {
        fConversation = ConversationFragment()
        fConversation!!.arguments = bundle
        if (fContent !is ContactRequestsFragment) {
            selectNavigationItem(R.id.navigation_home)
        }
        showTabletToolbar()
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.conversation_container,
                fConversation!!,
                ConversationFragment::class.java.simpleName
            )
            .commit()
    }

    private fun presentTrustRequestFragment(accountID: String?) {
        mNotificationService.cancelTrustRequestNotification(accountID)
        if (fContent is ContactRequestsFragment) {
            (fContent as ContactRequestsFragment).presentForAccount(accountID)
            return
        }
        val bundle = Bundle()
        bundle.putString(ContactRequestsFragment.ACCOUNT_ID, accountID)
        val content = ContactRequestsFragment()
        content.arguments = bundle
        fContent = content
        mBinding!!.navigationView.menu.getItem(NAVIGATION_CONTACT_REQUESTS).isChecked = true
        supportFragmentManager.beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(R.id.main_frame, content, CONTACT_REQUESTS_TAG)
            .addToBackStack(CONTACT_REQUESTS_TAG).commit()
    }

    override fun onBackPressed() {
        if (mAccountFragmentBackHandlerInterface != null && mAccountFragmentBackHandlerInterface!!.onBackPressed()) {
            return
        }
        super.onBackPressed()
        fContent = supportFragmentManager.findFragmentById(R.id.main_frame)
        if (fContent is SmartListFragment) {
            mBinding!!.navigationView.menu.getItem(NAVIGATION_CONVERSATIONS).isChecked =
                true
            //showProfileInfo();
            showToolbarSpinner()
            hideTabletToolbar()
        }
    }

    private fun popCustomBackStack() {
        val fragmentManager = supportFragmentManager
        val entryCount = fragmentManager.backStackEntryCount
        for (i in 0 until entryCount) {
            fragmentManager.popBackStack()
        }
        //fContent = fragmentManager.findFragmentById(R.id.main_frame);
        hideTabletToolbar()
        setToolbarElevation(false)
    }

    fun goToSettings() {
        if (fContent is SettingsFragment) {
            return
        }
        popCustomBackStack()
        hideToolbarSpinner()
        val content = SettingsFragment()
        fContent = content
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(fragmentContainerId, content, SETTINGS_TAG)
            .addToBackStack(SETTINGS_TAG).commit()
    }

    fun goToAbout() {
        if (fContent is AboutFragment) {
            return
        }
        popCustomBackStack()
        hideToolbarSpinner()
        val content = AboutFragment()
        fContent = content
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(fragmentContainerId, content, ABOUT_TAG)
            .addToBackStack(ABOUT_TAG).commit()
    }

    fun goToVideoSettings() {
        if (fContent is VideoSettingsFragment) {
            return
        }
        val content = VideoSettingsFragment()
        fContent = content
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(fragmentContainerId, content, VIDEO_SETTINGS_TAG)
            .addToBackStack(VIDEO_SETTINGS_TAG).commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val account = mAccountService.currentAccount ?: return false
        val bundle = Bundle()
        val itemId = item.itemId
        if (itemId == R.id.navigation_requests) {
            if (fContent is ContactRequestsFragment) {
                (fContent as ContactRequestsFragment).presentForAccount(null)
                return true
            }
            popCustomBackStack()
            val content = ContactRequestsFragment()
            fContent = content
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.main_frame, content, CONTACT_REQUESTS_TAG)
                .setReorderingAllowed(true)
                .addToBackStack(CONTACT_REQUESTS_TAG)
                .commit()
            //showProfileInfo();
            showToolbarSpinner()
        } else if (itemId == R.id.navigation_home) {
            if (fContent is SmartListFragment) {
                return true
            }
            popCustomBackStack()
            val content = SmartListFragment()
            fContent = content
            supportFragmentManager.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .replace(R.id.main_frame, content, HOME_TAG)
                .setReorderingAllowed(true)
                .commit()
            //showProfileInfo();
            showToolbarSpinner()
        } else if (itemId == R.id.navigation_settings) {
            if (account.needsMigration()) {
                Log.d(TAG, "launchAccountMigrationActivity: Launch account migration activity")
                val intent = Intent()
                    .setClass(this, AccountWizardActivity::class.java)
                    .setData(
                        android.net.Uri.withAppendedPath(
                            ContentUriHandler.ACCOUNTS_CONTENT_URI,
                            account.accountID
                        )
                    )
                startActivityForResult(intent, 1)
            } else {
                Log.d(TAG, "launchAccountEditFragment: Launch account edit fragment")
                bundle.putString(AccountEditionFragment.ACCOUNT_ID_KEY, account.accountID)
                if (fContent is AccountEditionFragment) {
                    return true
                }
                popCustomBackStack()
                val content = AccountEditionFragment()
                content.arguments = bundle
                fContent = content
                supportFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(fragmentContainerId, content, ACCOUNTS_TAG)
                    .addToBackStack(ACCOUNTS_TAG)
                    .commit()
                //showProfileInfo();
                showToolbarSpinner()
            }
        }
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        val type = mAccountAdapter!!.getItemViewType(position)
        if (type == AccountSpinnerAdapter.TYPE_ACCOUNT) {
            val account = mAccountAdapter!!.getItem(position)
            mAccountService.currentAccount = account
            showAccountStatus(fContent is AccountEditionFragment && !account!!.isSip)
        } else {
            val intent = Intent(this@HomeActivity, AccountWizardActivity::class.java)
            if (type == AccountSpinnerAdapter.TYPE_CREATE_SIP) {
                intent.action = AccountConfig.ACCOUNT_TYPE_SIP
            }
            startActivity(intent)
            mBinding!!.spinnerToolbar.setSelection(mAccountService.currentAccountIndex)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
    fun setBadge(menuId: Int, number: Int) {
        if (number == 0) mBinding!!.navigationView.removeBadge(menuId) else mBinding!!.navigationView.getOrCreateBadge(
            menuId
        ).number = number
    }

    private fun hideTabletToolbar() {
        if (mBinding != null && mBinding!!.tabletToolbar != null) {
            mBinding!!.contactTitle?.text = null
            mBinding!!.contactSubtitle?.text = null
            mBinding!!.contactImage!!.setImageDrawable(null)
            mBinding!!.tabletToolbar!!.visibility = View.GONE
        }
    }

    private fun showTabletToolbar() {
        if (mBinding != null && mBinding!!.tabletToolbar != null && DeviceUtils.isTablet(this)) {
            mBinding!!.tabletToolbar!!.visibility = View.VISIBLE
        }
    }

    fun setTabletTitle(@StringRes titleRes: Int) {
        if (mBinding!!.tabletToolbar != null) {
            mBinding!!.tabletToolbar!!.visibility = View.VISIBLE
            mBinding!!.contactTitle!!.setText(titleRes)
            mBinding!!.contactTitle!!.textSize = 19f
            mBinding!!.contactTitle!!.setTypeface(null, Typeface.BOLD)
            mBinding!!.contactImage!!.visibility = View.GONE
        }
        /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) binding.contactTitle.getLayoutParams();
        params.removeRule(RelativeLayout.ALIGN_TOP);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        binding.contactTitle.setLayoutParams(params);*/
    }

    fun setToolbarTitle(@StringRes titleRes: Int) {
        if (DeviceUtils.isTablet(this)) {
            setTabletTitle(titleRes)
        } else {
            setToolbarState(titleRes)
        }
    }

    fun showAccountStatus(show: Boolean) {
        mBinding!!.accountSwitch.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showToolbarSpinner() {
        mBinding!!.spinnerToolbar.visibility = View.VISIBLE
    }

    private fun hideToolbarSpinner() {
        if (mBinding != null && !DeviceUtils.isTablet(this)) {
            mBinding!!.spinnerToolbar.visibility = View.GONE
        }
    }

    private val fragmentContainerId: Int
        get() = if (DeviceUtils.isTablet(this@HomeActivity))
            R.id.conversation_container else R.id.main_frame

    fun setAccountFragmentOnBackPressedListener(backPressedListener: BackHandlerInterface?) {
        mAccountFragmentBackHandlerInterface = backPressedListener
    }

    /**
     * Changes the current main fragment to a plugins list settings fragment
     */
    fun goToPluginsListSettings() {
        if (fContent is PluginsListSettingsFragment) {
            return
        }
        val content = PluginsListSettingsFragment()
        fContent = content
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(fragmentContainerId, content, PLUGINS_LIST_SETTINGS_TAG)
            .addToBackStack(PLUGINS_LIST_SETTINGS_TAG).commit()
    }

    /**
     * Changes the current main fragment to a plugin settings fragment
     * @param pluginDetails
     */
    fun gotToPluginSettings(pluginDetails: PluginDetails?) {
        if (fContent is PluginSettingsFragment) {
            return
        }
        val content = PluginSettingsFragment.newInstance(pluginDetails)
        fContent = content
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(fragmentContainerId, content, PLUGIN_SETTINGS_TAG)
            .addToBackStack(PLUGIN_SETTINGS_TAG).commit()
    }

    /**
     * Changes the current main fragment to a plugin PATH preference fragment
     */
    fun gotToPluginPathPreference(pluginDetails: PluginDetails?, preferenceKey: String?) {
        if (fContent is PluginPathPreferenceFragment) {
            return
        }
        val content = PluginPathPreferenceFragment.newInstance(pluginDetails, preferenceKey)
        fContent = content
        supportFragmentManager
            .beginTransaction()
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(fragmentContainerId, content, PLUGIN_PATH_PREFERENCE_TAG)
            .addToBackStack(PLUGIN_PATH_PREFERENCE_TAG).commit()
    }

    override fun setColor(color: Int) {
        //mToolbar.setBackground(new ColorDrawable(color));
    }

    fun setToolbarElevation(enable: Boolean) {
        if (mBinding != null) mBinding!!.appBar.elevation = if (enable) resources.getDimension(R.dimen.toolbar_elevation) else 0f
    }

    fun setToolbarOutlineState(enabled: Boolean) {
        if (mBinding != null) {
            if (!enabled) {
                mBinding!!.appBar.outlineProvider = null
            } else {
                mBinding!!.appBar.outlineProvider = mOutlineProvider
            }
        }
    }

    fun popFragmentImmediate() {
        val fm = supportFragmentManager
        fm.popBackStackImmediate()
        val entry = fm.getBackStackEntryAt(fm.backStackEntryCount - 1)
        fContent = fm.findFragmentById(entry.id)
    }

    private fun selectNavigationItem(id: Int) {
        if (mBinding != null) mBinding!!.navigationView.selectedItemId = id
    }

    private fun enableAccount(newValue: Boolean) {
        val account = mAccountService.currentAccount
        if (account == null) {
            Log.w(TAG, "account not found!")
            return
        }
        account.isEnabled = newValue
        mAccountService.setAccountEnabled(account.accountID, newValue)
    }

    val switchButton: SwitchButton
        get() = mBinding!!.accountSwitch

    private fun setShareShortcuts(conversations: Collection<Conversation>) {
        val targetSize = (AvatarFactory.SIZE_NOTIF * resources.displayMetrics.density).toInt()
        var i = 0
        var maxCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(this)
        if (maxCount == 0) maxCount = 4
        val futureIcons: MutableList<Future<Bitmap>> =
            ArrayList(conversations.size.coerceAtMost(maxCount))
        for (conversation in conversations) {
            futureIcons.add(
                (mContactService as ContactServiceImpl?)!!.loadConversationAvatar(this,conversation)
                    .map { d: Drawable? -> BitmapUtils.drawableToBitmap(d, targetSize) }
                    .subscribeOn(Schedulers.computation())
                    .toFuture())
            if (++i == maxCount) break
        }
        val shortcutInfoList: MutableList<ShortcutInfoCompat> = ArrayList(futureIcons.size)
        i = 0
        for (conversation in conversations) {
            var icon: IconCompat? = null
            try {
                icon = IconCompat.createWithBitmap(futureIcons[i].get())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load icon", e)
            }
            val path = ConversationPath(conversation)
            val key = path.toKey()
            val person = Person.Builder()
                .setName(conversation.title)
                .setKey(key)
                .build()
            val shortcutInfo = ShortcutInfoCompat.Builder(this, key)
                .setShortLabel(conversation.title)
                .setPerson(person)
                .setLongLived(true)
                .setIcon(icon)
                .setCategories(setOf(CONVERSATIONS_CATEGORY))
                .setIntent(
                    Intent(
                        Intent.ACTION_SEND,
                        android.net.Uri.EMPTY,
                        this,
                        HomeActivity::class.java
                    )
                        .putExtras(path.toBundle())
                )
                .build()
            shortcutInfoList.add(shortcutInfo)
            if (++i == maxCount) break
        }
        try {
            Log.d(TAG, "Adding shortcuts: " + shortcutInfoList.size)
            ShortcutManagerCompat.removeAllDynamicShortcuts(this)
            ShortcutManagerCompat.addDynamicShortcuts(this, shortcutInfoList)
        } catch (e: Exception) {
            Log.w(TAG, "Error adding shortcuts", e)
        }
    }

    companion object {
        val TAG: String = HomeActivity::class.simpleName!!
        const val REQUEST_CODE_CALL = 3
        const val REQUEST_CODE_CONVERSATION = 4
        const val REQUEST_CODE_PHOTO = 5
        const val REQUEST_CODE_GALLERY = 6
        const val REQUEST_CODE_QR_CONVERSATION = 7
        const val REQUEST_PERMISSION_CAMERA = 113
        const val REQUEST_PERMISSION_READ_STORAGE = 114
        private const val NAVIGATION_CONTACT_REQUESTS = 0
        private const val NAVIGATION_CONVERSATIONS = 1
        private const val NAVIGATION_ACCOUNT = 2
        const val HOME_TAG = "Home"
        const val CONTACT_REQUESTS_TAG = "Trust request"
        const val ACCOUNTS_TAG = "Accounts"
        const val ABOUT_TAG = "About"
        const val SETTINGS_TAG = "Prefs"
        const val VIDEO_SETTINGS_TAG = "VideoPrefs"
        const val ACTION_PRESENT_TRUST_REQUEST_FRAGMENT =
            BuildConfig.APPLICATION_ID + "presentTrustRequestFragment"
        const val PLUGINS_LIST_SETTINGS_TAG = "PluginsListSettings"
        const val PLUGIN_SETTINGS_TAG = "PluginSettings"
        const val PLUGIN_PATH_PREFERENCE_TAG = "PluginPathPreference"
        private const val CONVERSATIONS_CATEGORY = "conversations"
    }
}